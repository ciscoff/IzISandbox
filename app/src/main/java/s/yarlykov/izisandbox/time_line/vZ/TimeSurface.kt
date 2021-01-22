package s.yarlykov.izisandbox.time_line.vZ

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View.MeasureSpec.EXACTLY
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import io.reactivex.disposables.CompositeDisposable
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.dsl.extenstions.dp_f
import s.yarlykov.izisandbox.extensions.minutes
import s.yarlykov.izisandbox.time_line.domain.*
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

class TimeSurface @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr),
    TimeDataConsumer {

    init {
        // Разрешить рисование для ViewGroup
        setWillNotDraw(false)
    }

    companion object {
        const val INVALID_POINTER_ID = -1
        const val BACKGROUND_HEIGHT_RATIO = 0.7f
        const val SLOT_SIZE_MIN = 5

        // Параметры для рисования метрик (линий/цифр)
        const val METRICS_STROKE_WIDTH_DP = 1.4f
        const val METRICS_TEXT_SIZE_SP = 11f
        const val SEPARATOR_TOP_PADDING = 2f
        const val SEPARATOR_STROKE_WIDTH_DP = 0.8f
        const val STICKY_STROKE_WIDTH_DP = 2.6f
        const val STICKY_FRAME_CORNER_RADIUS_DP = 3f
    }

    /**
     * Направление мультитача
     */
    private enum class Direction {
        Same,
        Opposite
    }

    /**
     * Палец левый/правый
     */
    private enum class Pointer {
        Left,
        Right
    }

    /**
     * Положение фрейма относительно голубых регионов
     * @Overlapping: регион внутри фрейма
     * @Overlapped: фрейм внутри региона
     * @Intersect: фрейм и регион пересекаются одной стороной
     * @Neighbor: фрейм и регион не пересекаются, соседствуют
     * @None: регионов нет
     */
    enum class Relationship {
        Overlapping,
        Overlapped,
        Intersect,
        Neighbor,
        None
    }

    private val disposable = CompositeDisposable()

    /**
     * Отправлять данные об изменении положения/размера ползунка
     */
    private var timeChangeListener: ((Int, Int) -> Unit)? = null

    /**
     * Величины высот основных компонентов
     */
    private var backgroundHeight = 0f
    private var metricsLineHeight = 0f
    private var metricsTextHeight = 0f

    /**
     * Признак слотовости и размер слота
     */
    private var slotType = TimeSlotType.NoSlotable
    private var slotSize = SLOT_SIZE_MIN

    /**
     * Режим UI (master/client)
     */
    private lateinit var severityMode: SeverityMode

    /**
     * Голубые сегменты. Координаты в минутах (не в px)
     */
    private val segments = mutableListOf<Segment>()

    /**
     * Элементы для рисования
     */
    private lateinit var cacheBitmap: Bitmap
    private lateinit var cacheCanvas: Canvas

    private val paintBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val paintText = Paint().apply {
        color = ContextCompat.getColor(context, R.color.color_time_line_chart)
        strokeWidth = dp_f(METRICS_STROKE_WIDTH_DP)
        textSize = dp_f(METRICS_TEXT_SIZE_SP)
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val paintSlot = Paint().apply {
        color = Color.WHITE
        strokeWidth = dp_f(SEPARATOR_STROKE_WIDTH_DP)
        isAntiAlias = true
    }

    private val paintSticky = Paint().apply {
        color = ContextCompat.getColor(context, R.color.colorDecor16)
        strokeWidth = dp_f(STICKY_STROKE_WIDTH_DP)
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val bgColor = ContextCompat.getColor(context, R.color.colorTimeSlotBusy)
    private val fgColor = ContextCompat.getColor(context, R.color.colorTimeSlotFree)

    /**
     * Элементы для позиционирования ползунка
     */

    // Начало и конец рабочего дня В МИНУТАХ
    private var low = 1
        set(value) {
            field = value.minutes
        }
    private var high = 1
        set(value) {
            field = value.minutes
        }

    private var dayRange = (1..1)

    // Единица времени: 1 минута
    private val timeUnit = 1

    // Это количество пикселей на одну минуту (mip по аналогии с dip)
    private var mip: Float = timeUnit.toFloat()

    /**
     * Для обработки событий onTouch
     */
    private var activePointerId = 0
    private var frameX = 0f
    private var scaleFactor = 1f

    private val pointers = mutableMapOf<Int, Float>()
    private val points = mutableMapOf(Pointer.Left to 0f, Pointer.Right to 0f)

    private lateinit var timeFrame: TimeFrame

    /**
     * У меня пока размеры задаются жестко. Поэтому вариант WRAP_CONTENT не учитываем.
     *
     * Ползунок принудительно устанавливается в высоту myHeight * BACKGROUND_HEIGHT_RATIO
     *
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val myHeight = MeasureSpec.getSize(heightMeasureSpec)

        val (childX, childY) = 0 to 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)

            if (child is TimeFrame) {
                measureChild(
                    child,
                    widthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(
                        (myHeight * BACKGROUND_HEIGHT_RATIO).toInt(),
                        EXACTLY
                    )
                )

                timeFrame = child
                val params = child.layoutParams as LayoutParams

                params.x = childX
                params.y = childY
            } else {
                measureChild(child, widthMeasureSpec, heightMeasureSpec)
            }
        }

        setMeasuredDimension(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val params = child.layoutParams as LayoutParams
            child.layout(params.x, params.y, child.measuredWidth, child.measuredHeight)
        }

        calculateDimensions()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        if (!disposable.isDisposed) {
            disposable.clear()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.actionMasked) {

            // Самое первое касание в жесте.
            MotionEvent.ACTION_DOWN -> {
                pointers.clear()
                activePointerId = event.getPointerId(0)
                pointers[activePointerId] = event.getX(0)
            }

            // Касание вторым пальцем. Теперь оба пальца на экране.
            MotionEvent.ACTION_POINTER_DOWN -> {
                event.actionIndex.also { index ->
                    pointers[event.getPointerId(index)] = event.getX(index)
                }
                savePoints(event)
            }

            // Уже в процессе. Мы все время ориентируемся на основной pointer, который
            // отслеживается по activePointerId.
            MotionEvent.ACTION_MOVE -> {
                val x = event.findPointerIndex(activePointerId)
                    .let { index -> event.getX(index) }

                when (event.pointerCount) {
                    1 -> {
                        translateFrame(x - pointers[activePointerId]!!)
                        pointers[activePointerId] = x
                    }
                    2 -> {
                        when (direction(event)) {
                            Direction.Same -> {
                                translateFrame(x - pointers[activePointerId]!!)
                                savePointers(event)
                                savePoints(event)
                            }
                            Direction.Opposite -> {
                                resizeFrame(event)
                                savePointers(event)
                                savePoints(event)
                            }
                        }
                    }
                }
            }

            /**
             * Какой-то палец поднят (но не последний). Если это обладатель activePointerId, то нужно
             * оставшийся палец назначить на роль основного
             */
            MotionEvent.ACTION_POINTER_UP -> {
                event.actionIndex.also { index ->
                    event.getPointerId(index).takeIf { it == activePointerId }?.run {
                        val newIndex = if (index == 0) 1 else 0
                        activePointerId = event.getPointerId(newIndex)
                        pointers[activePointerId] = event.getX(newIndex)
                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activePointerId = INVALID_POINTER_ID
                turnFrameBounds()
            }
        }

        return true
    }

    /**
     * Определить направление движения пальцев: в одном направлении или в противоположном.
     * Если направление противоположное, то это scale. Направление считается противоположным и в
     * случае, если один палец на месте, а второй двигается.
     */
    private fun direction(event: MotionEvent): Direction {
        val (indexL, indexR) = pointsIndices(event)

        val (currentL, prevL) = event.getX(indexL) to pointers[event.getPointerId(indexL)]!!
        val (currentR, prevR) = event.getX(indexR) to pointers[event.getPointerId(indexR)]!!

        return if (sign((currentL - prevL) * (currentR - prevR)) <= 0) Direction.Opposite else Direction.Same
    }

    /**
     * Переместить ползунок
     */
    private fun translateFrame(dX: Float) {

        if (!this::timeFrame.isInitialized) return

        frameX += dX

        /**
         * Ползунок не должен вылезать за края родительского элемента
         */
        when {
            (frameX < 0) -> {
                frameX = 0f
            }
            (frameX > measuredWidth - timeFrame.measuredWidth) -> {
                frameX = (measuredWidth - timeFrame.measuredWidth).toFloat()
            }
        }

        timeFrame.translationX = frameX

        sendTimeUpdate()
    }

    /**
     * Отправить данные в listener внешним потребителям
     */
    private fun sendTimeUpdate() {

        val l = (timeFrame.translationX / mip).toInt()
        val h = ((timeFrame.translationX + timeFrame.width) / mip).toInt()

        timeChangeListener?.invoke(l, h)
    }

    /**
     * Сохранить координаты левого и правого указателей.
     *
     * Функция исползует содержимое структуры pointers, поэтому перед её вызовом необходимо
     * поместить в pointers последнюю актуальную информацию (savePointers)
     */
    private fun savePoints(event: MotionEvent) {
        val (l, r) = pointsIndices(event)

        points[Pointer.Left] = event.getX(l)
        points[Pointer.Right] = event.getX(r)
    }

    /**
     * Сохранить координаты активного и пассивного указателей
     */
    private fun savePointers(event: MotionEvent) {
        val (a, p) = pointersIndices(event)

        val passivePointerId = event.getPointerId(p)
        pointers[activePointerId] = event.getX(a)
        pointers[passivePointerId] = event.getX(p)
    }

    /**
     * Определить индексы активного и пассивного указателей
     */
    private fun pointersIndices(event: MotionEvent): Pair<Int, Int> {
        val indexActive = event.findPointerIndex(activePointerId)
        val indexPassive = if (indexActive == 0) 1 else 0

        return indexActive to indexPassive
    }

    /**
     * Определить индексы левого и правого указателей
     */
    private fun pointsIndices(event: MotionEvent): Pair<Int, Int> {
        val lastTouch0 = pointers[event.getPointerId(0)]!!
        val lastTouch1 = pointers[event.getPointerId(1)]!!

        val indexL = if (lastTouch0 < lastTouch1) 0 else 1
        val indexR = if (lastTouch0 < lastTouch1) 1 else 0

        return indexL to indexR
    }

    /**
     * В этом методе важно различать левый и правый указатели.
     * Active/Passive роли не играет
     */
    private fun resizeFrame(event: MotionEvent) {

        val (indexL, indexR) = pointsIndices(event)
        val (xL, xR) = event.getX(indexL) to event.getX(indexR)

        val prevSpan = points[Pointer.Right]!! - points[Pointer.Left]!!
        val currentSpan = xR - xL

        val factor = currentSpan / prevSpan

        // Важно ! Скалирование именно так изменять.
        scaleFactor *= factor

        val frameWidthBefore = timeFrame.measuredWidth

        // Ползунок не может быть шире родителя
        var frameWidthAfter =
            min((frameWidthBefore * factor).toInt(), (timeFrame.parent as ViewGroup).measuredWidth)

        // Ползунок не может быть меньше минимума
        frameWidthAfter = max((slotSize * mip).toInt(), frameWidthAfter)

        // Нужно подвинуть левый край левее на половину изменения ширины
        val tX = (frameWidthAfter - frameWidthBefore) / 2f
        translateFrame(-tX)

        timeFrame.layoutParams = timeFrame.layoutParams.apply { width = frameWidthAfter }
    }

    /**
     * Функция выполняет расчет высоты для основных компонентов.
     * - высота текста определяется через FontMetrics
     * - высота цветового фона с помощью ratio
     * - оставшееся вертикальное пространство остается для высоты шкаликов метрической линейки
     */
    private fun calculateDimensions() {
        metricsTextHeight = paintText.fontMetrics.let { it.descent - it.ascent }
        backgroundHeight = height * BACKGROUND_HEIGHT_RATIO
        metricsLineHeight = height - backgroundHeight - metricsTextHeight
    }

    /**
     * ********************************************************************************
     * Drawing Block
     * ********************************************************************************
     */
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (::cacheBitmap.isInitialized) {
            canvas?.drawBitmap(cacheBitmap, 0f, 0f, null)
        }
    }

    /**
     * Отрисовка заднего фона и линейки
     */
    private fun drawInCache(timeData: TimeData, model: List<DateRange>) {
        if (::cacheBitmap.isInitialized) {
            cacheBitmap.recycle()
        }

        if (width > 0 && height > 0) {
            cacheBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            cacheCanvas = Canvas(cacheBitmap)
            cacheCanvas.drawColor(bgColor)

            paintBg.color = fgColor

            drawRectangles(timeData, model)
            drawLines()
            drawHours()
            drawStickyFrame(timeData)
            invalidate()
        }
    }

    /**
     * Цветной фон. Рисуется на всю высоту, потом нижняя часть для линейки заливается белым.
     */
    private fun drawRectangles(timeData: TimeData, model: List<DateRange>) {
        model.forEach { dateRange ->
            val (from, to) = dateRange.from.minutes to dateRange.to.minutes

            // Проверка, что полученные данные попадают в диапазон дня.
            // Также нужно вычесть значение low, т.к. нужны относительные значения внутри шкалы.
            val left = ((if (from in dayRange) from else low) - low) * mip
            val right = ((if (to in dayRange) to else high) - low) * mip
            val rect = Rect(left.toInt(), 0, right.toInt(), height)
            cacheCanvas.drawRect(rect, paintBg)
        }

        // Это белая заливка, поверх которой будут метрики (линии/текст)
        paintBg.color = Color.WHITE
        val rect = Rect(0, backgroundHeight.toInt(), width, height)
        cacheCanvas.drawRect(rect, paintBg)
    }

    /**
     * Рамка начального положения ползунка.
     *
     * @offset нужен для корректировки положения сторон прямоугольника, чтобы они не вылезали
     * за ширину основного фрейма. Канва цетрирует линию по толщине.
     */
    private fun drawStickyFrame(timeData: TimeData) {
        val offset = dp_f(STICKY_STROKE_WIDTH_DP) / 2
        val radius = dp_f(STICKY_FRAME_CORNER_RADIUS_DP)

        val leftX = (timeData.frameStartPosition.from - timeData.startHour).minutes * mip
        val rightX = leftX + (timeData.itemDuration * mip)

        val rect = RectF(
            leftX + offset, offset, rightX - offset, backgroundHeight - offset
        )
        cacheCanvas.drawRoundRect(rect, radius, radius, paintSticky)
    }

    /**
     * Рисуем линейку
     */
    private fun drawLines() {

        val hours = (dayRange.last - dayRange.first) / 60
        if (hours == 0) return

        val startY = backgroundHeight
        val longY = startY + metricsLineHeight
        val shortY = startY + metricsLineHeight / 2

        // Один промежуток - полчаса
        val gaps = hours * 2
        // Количество вертикальных отсечек
        val lines = gaps + 1
        // Ширина промежутка в px
        val gap = width.toFloat() / gaps

        for (i in 0 until lines) {
            // floor нужно, чтобы все насечки были одинаковой толщины
            val startX = floor(i * gap)

            val y = if (i % 2 == 0) {

                // Дополнительно рисуем вертикальные разделители (для слотовой услуги),
                // но не у первого и последнего шкалика.
                if (i > 0 && i < (lines - 1)) {
                    drawSlotsSeparator(startX)
                }

                longY
            } else {
                shortY
            }

            cacheCanvas.drawLine(startX, startY, startX, y, paintText)
        }

        // Горизонтальная линия на всю ширину
        cacheCanvas.drawLine(0f, startY, width.toFloat(), startY, paintText)
    }

    /**
     * Рисуем разделитель слотов
     */
    private fun drawSlotsSeparator(x: Float) {
        if (slotType != TimeSlotType.NoSlotable) {
            cacheCanvas.drawLine(x, dp_f(SEPARATOR_TOP_PADDING), x, backgroundHeight, paintSlot)
        }
    }

    /**
     * Рисуем числа. Они центрируются относительно отсечек благодаря настройке текстовой Paint.
     */
    private fun drawHours() {
        val hours = (dayRange.last - dayRange.first) / 60
        if (hours == 0) return

        val steps = hours - 1
        val gap = width.toFloat() / hours

        /**
         * Позиционируем канву для отрисовки текста. Позиция канвы станет левым-НИЖНИМ углом
         * области текста. Не левым-ВЕРХНИМ, а левым-НИЖНИМ, то есть все через жопу. Поэтому
         * ставим канву на нижнюю границу View, а текст отрусуется поверх этой границы.
         *
         * Полезная заметка тут:
         * https://stackoverflow.com/questions/3654321/measuring-text-height-to-be-drawn-on-canvas-android
         */
        cacheCanvas.save()
        cacheCanvas.translate(0f, height.toFloat())

        for (i in 1..steps) {
            cacheCanvas.translate(gap, 0f)
            cacheCanvas.drawText("${(dayRange.first + i * 60) / 60}", 0f, 0f, paintText)
        }

        cacheCanvas.restore()
    }

    /**
     * Начальное позиционирование ползунка по данным из TimeData
     *
     * NOTE: Сначала layout потом translate, иначе могут возникать разного рода траблы.
     */
    private fun setSliderInitPosition(timeData: TimeData) {

        if (this::timeFrame.isInitialized) {
            frameX = (timeData.frameStartPosition.from - timeData.startHour).minutes * mip

            timeFrame.layoutParams =
                timeFrame.layoutParams.apply { width = (timeData.itemDuration * mip).toInt() }

            postDelayed({ translateFrame(0f) }, 10)
        }
    }

    /**
     * ********************************************************************************
     * User Interaction Block
     * ********************************************************************************
     *
     * Скорректировать положение и размеры ползунка
     */
    private fun turnFrameBounds() {
        val left = (timeFrame.translationX / mip).toInt()
        val right = ((timeFrame.translationX + timeFrame.width.toFloat()) / mip).toInt()
        val frame = Segment(left, right)

        val (relationship, neighbor) = neighborsRelationship(frame)

        when (relationship) {
            Relationship.Overlapping -> {
                envelopBounds(neighbor!!)
            }
            Relationship.Overlapped -> {
                embedBounds(frame, neighbor!!)
            }
            Relationship.Intersect, Relationship.Neighbor -> {
                alignBounds(frame, neighbor!!)
            }
            Relationship.None -> {
            }
        }
    }

    /**
     * Определить отношение с соседними сегментами (перекрытия/пересечения/соседство)
     */
    private fun neighborsRelationship(frame: Segment): Pair<Relationship, Segment?> {

        // Фрейм может одновременно полностью накрывать несколько голубых регионов
        val overlaps = segments.filter { other -> frame.overlapping(other) }
        frame.findCloser(overlaps)?.let {
            return Relationship.Overlapping to it
        }

        // Фрейм может находиться только внутри одного региона
        segments.firstOrNull { other ->
            frame.overlapped(other)
        }?.let {
            return Relationship.Overlapped to it
        }

        // Фрейм может пересекаться с несколькими регионами (2-мя)
        val intersections = segments.filter { other -> frame.intersect(other) }
        frame.findCloser(intersections)?.let {
            return Relationship.Intersect to it
        }

        frame.neighbor(segments)?.let {
            return Relationship.Neighbor to it
        }

        return Relationship.None to null
    }

    /**
     * Установить свою позицию и размеры по контуру охватываемого региона
     *
     * NOTE: Для граничных условий (когда голубой регион на краю шкалы) нужно
     * шедулить translate, чтобы он выполнялся после layout.
     */
    private fun envelopBounds(innerSegment: Segment) {
        frameX = innerSegment.x1 * mip
        timeFrame.layoutParams =
            timeFrame.layoutParams.apply { width = (innerSegment.length * mip).toInt() }

        postDelayed({ translateFrame(0f) }, 10)
    }

    /**
     * Находясь внутри голубого региона меняем размеры. Конечный размер фрейма должен
     * быть кратен slotSize.
     */
    private fun embedBounds(_frame: Segment, _other: Segment) {

        // Нормализуем длину
        val frame = _frame.normalize(slotSize, _other.length)

        // Ищем к кому "прислониться"
        frame.findCloser(_other.splitOverlapped(frame.length, slotSize))?.let { other ->

            // Корректируем позицию и размер
            frameX = if (frame.center - other.x1 < other.x2 - frame.center) {
                other.x1 * mip
            } else {
                (other.x2 - frame.length) * mip
            }

            timeFrame.layoutParams =
                timeFrame.layoutParams.apply { width = (frame.length * mip).toInt() }

            postDelayed({ translateFrame(0f) }, 10)
        }
    }

    /**
     * Прижаться "внутри" региона other к ближайшей границе
     */
    private fun alignBounds(_frame: Segment, other: Segment) {

        // Нормализуем длину
        val frame = _frame.normalize(slotSize, other.length)

        // Мы слева от other
        frameX = if (frame.x1 < other.x1) {
            other.x1 * mip
        }
        // Мы справа от other
        else {
            (other.x2 - frame.length) * mip
        }

        timeFrame.layoutParams =
            timeFrame.layoutParams.apply { width = (frame.length * mip).toInt() }

        postDelayed({ translateFrame(0f) }, 10)
    }

    /**
     * Сконвертировать диапазоны свободного времени в сегменты.
     * Сегмент - это пара координат - начало и конец. Ед.изм. - МИНУТЫ.
     *
     * В режиме Client сегменты строго соответствуют диапазонам свободного времени.
     * В режиме Master весь диапазон дня разбивается на сегменты, кратные slotSize
     * и ползунок можно ставить в любую позицию.
     *
     * NOTE: В режиме Master получается, что нет Neighbors, а только Overlapping, потому
     * что все сегменты смежные, следуют друг за другом непрерывно.
     *
     */
    private fun mapHoursToSegments(_startHour: Int, _endHour: Int, _model: List<DateRange>) {

        val l = _startHour.minutes
        val h = _endHour.minutes
        val day = l..h

        segments.clear()

        val model = when (severityMode) {
            SeverityMode.Client -> _model
            SeverityMode.Master -> allDayModel(_startHour, _endHour, slotSize)
        }

        model.forEach { dateRange ->
            val (from, to) = dateRange.from.minutes to dateRange.to.minutes

            // Проверка, что полученные данные попадают в диапазон дня.
            // Также нужно вычитать значение low, т.к. нужны относительные значения внутри шкалы.
            val left = ((if (from in day) from else l) - l)
            val right = ((if (to in day) to else h) - l)
            segments.add(Segment(left, right))
        }
    }

    /**
     * Весь день разделить на смежные диапазоны продолжительностью step.
     */
    private fun allDayModel(startHour: Int, endHour: Int, step: Int): List<DateRange> {
        val model = mutableListOf<DateRange>()

        (startHour until endHour).forEach {
            model.add(DateRange(it, it + step))
        }

        return model
    }

    /**
     * ********************************************************************************
     * LayoutParams Block
     * ********************************************************************************
     *
     * Наш контейнер должен генерить LayoutParams для детей
     */
    override fun generateLayoutParams(attrs: AttributeSet?): ViewGroup.LayoutParams {
        return LayoutParams(
            context,
            attrs
        )
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
        return LayoutParams(p)
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        return p is LayoutParams
    }

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return LayoutParams()
    }

    /**
     * Кастомные layoutParams
     */
    class LayoutParams : ViewGroup.LayoutParams {
        var x = 0
        var y = 0

        @JvmOverloads
        constructor(
            width: Int = MATCH_PARENT,
            height: Int = MATCH_PARENT
        ) : super(width, height)

        constructor(context: Context?, attrs: AttributeSet?) : super(
            context,
            attrs
        )

        constructor(params: ViewGroup.LayoutParams) : super(params)
    }

    /**
     * ********************************************************************************
     * ViewModel Block
     * ********************************************************************************
     *
     * Получение данных из ViewModel от родительского контейнера
     */
    private fun timeDataHandler(timeData: TimeData) {
        val hoursQty = timeData.hoursQty
        low = timeData.startHour
        high = timeData.endHour
        dayRange = (low..high)
        slotType = timeData.timeSlotType
        slotSize = timeData.timeSlotValue
        mip = (measuredWidth.toFloat() / (hoursQty.minutes)) * timeUnit

        setSliderInitPosition(timeData)
    }

    override fun initialize(
        _timeData: TimeData,
        _schedule: List<DateRange>,
        _severityMode: SeverityMode
    ) {
        severityMode = _severityMode

        postDelayed({
            timeDataHandler(_timeData)
            drawInCache(_timeData, _schedule)
            mapHoursToSegments(_timeData.startHour, _timeData.endHour, _schedule)
        }, 10)
    }

    override fun setOnTimeChangeListener(listener: (Int, Int) -> Unit) {
        timeChangeListener = listener
    }
}