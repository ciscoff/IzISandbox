package s.yarlykov.izisandbox.time_line.v2

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View.MeasureSpec.EXACTLY
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.dsl.extenstions.dp_f
import s.yarlykov.izisandbox.extensions.minutes
import s.yarlykov.izisandbox.time_line.TimeLineView
import s.yarlykov.izisandbox.time_line.domain.DateRange
import s.yarlykov.izisandbox.time_line.domain.TimeData
import s.yarlykov.izisandbox.time_line.domain.TimeSlotType
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sign

class TimeSurfaceV2 : ViewGroup, TimeLineView {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        // Разрешить рисование для ViewGroup
        setWillNotDraw(false)
    }

    companion object {
        const val INVALID_POINTER_ID = -1
        const val BACKGROUND_HEIGHT_RATIO = 0.7f

        // Параметры для рисования метрик (линий/цифр)
        const val METRICS_STROKE_WIDTH_DP = 1.4f
        const val METRICS_TEXT_SIZE_SP = 11f
        const val SEPARATOR_TOP_PADDING = 2f
        const val SEPARATOR_STROKE_WIDTH_DP = 0.8f
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

    private val disposable = CompositeDisposable()

    /**
     * Величины высот основных компонентов
     */
    private var backgroundHeight = 0f
    private var metricsLineHeight = 0f
    private var metricsTextHeight = 0f

    /**
     * Признак слотовости
     */

    private var slotType = TimeSlotType.NoSlotable

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

    private lateinit var timeFrame: TimeFrameV2

    // Можно удалить
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setBackgroundColor(
            ContextCompat.getColor(
                context, R.color.colorDecor14
            )
        )
    }

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

            if (child is TimeFrameV2) {
                measureChild(
                    child,
                    widthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(
                        (myHeight * BACKGROUND_HEIGHT_RATIO).toInt(),
                        EXACTLY
                    )
                )

                timeFrame = child
                val params = timeFrame.layoutParams as LayoutParams

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
        translateFrame(0f)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (::cacheBitmap.isInitialized) {
            canvas?.drawBitmap(cacheBitmap, 0f, 0f, null)
        }
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
            // отслеживается по activePointerId
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

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activePointerId =
                    INVALID_POINTER_ID
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

        scaleFactor *= factor

        val frameWidthBefore = timeFrame.measuredWidth
        // Ползунок не может быть шире родителя
        val frameWidthAfter =
            min((frameWidthBefore * factor).toInt(), (timeFrame.parent as ViewGroup).measuredWidth)

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
     * Отрисовка заднего фона
     */
    private fun drawInCache(model: List<DateRange>) {
        if (::cacheBitmap.isInitialized) {
            cacheBitmap.recycle()
        }

        cacheBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        cacheCanvas = Canvas(cacheBitmap)
        cacheCanvas.drawColor(bgColor)

        paintBg.color = fgColor

        drawRectangles(model)
        drawLines()
        drawHours()
        invalidate()
    }

    /**
     * Цветной фон. Рисуется на всю высоту, потом нижняя часть для линейки закрашиывается белым.
     */
    private fun drawRectangles(model: List<DateRange>) {
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
        val rect = Rect(0, (backgroundHeight/*height * BG_HEIGHT_RATIO*/).toInt(), width, height)
        cacheCanvas.drawRect(rect, paintBg)
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
     * Рисуем числа. Они центрируются относительно отсечек благодаря настройки текстовой Paint.
     */
    private fun drawHours() {
        val hours = (dayRange.last - dayRange.first) / 60
        if (hours == 0) return

        val steps = hours - 1
        val gap = width.toFloat() / hours

        /**
         * Позиционируем канву для отрисовки текста. Позиция канвы станет левым-НИЖНИМ углом
         * области текста. Не левым-ВЕРХНИМ, а левым-НИЖНИМ, то есть все через жопу. Поэтому
         * ставим канву на нижнюю границы View, а текст отрусуется поверх этой границы.
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
     */
    private fun frameInitPosition(timeData: TimeData) {
        frameX = (timeData.frameStartPosition.from - timeData.startHour).minutes * mip
        translateFrame(0f)

        timeFrame.layoutParams =
            timeFrame.layoutParams.apply { width = (timeData.itemDuration * mip).toInt() }
    }

    /**
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
     * Имплементация TimeLineView
     */
    private fun timeDataHandler(timeData: TimeData) {
        val hoursQty = timeData.hoursQty
        low = timeData.startHour
        high = timeData.endHour
        dayRange = (low..high)
        slotType = timeData.timeSlotType
        mip = (measuredWidth.toFloat() / (hoursQty.minutes)) * timeUnit

        frameInitPosition(timeData)
    }

    private fun schedulerHandler(model: List<DateRange>) {
        drawInCache(model)
    }

    override fun onTimeData(timeDataObs: Observable<TimeData>) {
        disposable += timeDataObs
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::timeDataHandler)
    }

    override fun onSchedulerData(scheduleDataObs: Observable<List<DateRange>>) {
        disposable += scheduleDataObs
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::schedulerHandler)
    }
}