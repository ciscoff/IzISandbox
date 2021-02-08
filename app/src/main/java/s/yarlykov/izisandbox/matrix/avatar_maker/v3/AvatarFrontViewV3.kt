package s.yarlykov.izisandbox.matrix.avatar_maker.v3

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import s.yarlykov.izisandbox.dsl.extenstions.dp_f
import s.yarlykov.izisandbox.extensions.center
import s.yarlykov.izisandbox.extensions.scale
import s.yarlykov.izisandbox.matrix.avatar_maker.gesture.*
import s.yarlykov.izisandbox.utils.logIt
import kotlin.math.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Алгоритм zoom'а такой:
 * 1. Минимально доступный размер viewPort'а выбирается равным 3/5 от наименьшей стороны view.
 * 2. Если ПОСЛЕ тача viewPort стал меньше минимума, то:
 *   - битмапа увеличивается в том же оношении, что и viewPort меньше минимума.
 *   - viewPort анимированно увеличивается к минимальному размеру.
 *
 * Например, пусть минимальный размер viewPort'а равен W90xH90. Мы уменьшили его до W60xH60.
 * Получилось соотношение сторон начального и нового состояний как 60/90 = 2/3, то есть viewPort
 * уменьшился на 1/3. Это значит, что мы должны предыдущее состояния битмапы увеличить на 1/3.
 *
 * Однако нужно делать проверку, чтобы разрешение видимой части битмапы не превышало разрешения
 * области экрана, на которой она отображается. То есть битмапа не должна растягиваться больше
 * своей натуральной величины. Сжиматься может, а растягиваться - нет.
 *
 * Видимо понадобятся:
 * ScaleGestureDetector.OnScaleGestureListener
 * GestureDetector.OnGestureListener
 */

class AvatarFrontViewV3 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AvatarBaseViewV3(context, attrs, defStyleAttr) {

    private var mode: Mode = Mode.Waiting

    /**
     * Квадратные области для масштабирования рамки видоискателя
     */
    private val tapSquares: Map<TapArea, RectF> by rectTapDelegate()

    /**
     * Ширина линии рамки
     */
    private val borderWidth = dp_f(2f)

    /**
     * Радиус закругления рамки видоискателя
     */
    private val cornerRadius = dp_f(2f)

    /**
     * @rectPivot КВАДРАТ (в координатах канвы) выровненный по центру rectVisible.
     * Сторона квадрата равна наименьшей из сторон rectVisible.
     */
    private val rectPivot = RectF()

    /**
     * Это viewPort в координатах канвы(view).
     * @rectClip квадратная область для рисования рамки выбора части изображения под аватарку.
     */
    private var rectClip: RectF by rectClipDelegate()

    /**
     * Копия rectClip в момент ACTION_DOWN. В момент ACTION_UP отношение сторон
     * rectClip/rectClipPrev дает нам величину scale.
     */
    private val rectClipPrev = RectF()

    /**
     * @rectClipShifted служит для временного копирования rectClip в операциях скалирования
     */
    private val rectClipShifted = RectF()

    /**
     * Содержит минимально допустимый размер для viewPort'а в пассивном состоянии
     */
    private val rectMin: RectF by rectMinDelegate()

    /**
     * Для временных данных при работе анимации
     */
    private val rectTemp = RectF()

    /**
     * @rectBorder прямоугольник для рамки
     */
    private val rectBorder: RectF by rectBorderDelegate()

    /**
     * @pathClip определяется через @rectClip.
     */
    private val pathClip = Path()

    /**
     * @pathBorder для рамки "видоискателя"
     */
    private val pathBorder = Path()

    /**
     * Расстояние от точки касания до центра rectClip в предыдущем событии TouchEvent.
     * Если в текущем событии TouchEvent расстояние увеличилось, то растягиваем рамку, иначе
     * сжимаем. Используется в скалировании.
     */
    private var prevDistance: Float = 0f

    /**
     * @offsetV - это дополнительный вертикальный offset учитывающий жест перетаскивания
     * @offsetH - это дополнительный горизонтальный offset учитывающий жест перетаскивания
     */
    private var offsetV = 0f
    private var offsetH = 0f

    /**
     * Отслеживаем движения пальца в жестах
     */
    private var lastX = 0f
    private var lastY = 0f

    /**
     * Темный полупрозрачный цвет
     */
    private val darkShadeColor = Color.argb(0x88, 0x00, 0x00, 0x00)

    /**
     * Paint (color Yellow)
     */
    private val paintStroke: Paint = Paint().apply {
        color = Color.argb(0xff, 0xff, 0xff, 0x0)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = borderWidth
        isAntiAlias = true
    }

    /**
     * Минимально допустимая высота viewPort'а при текущем scaleRemain
     */
    private var minHeight = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {

        return when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                // Запомнить перед скалированием чтобы потом посчитать scale
                prevDistance = distanceToViewPortCenter(event.x, event.y)
                rectClipPrev.set(rectClip)

                isScaleDownAvailable = true
                isScaleUpAvailable = true

                lastX = event.x
                lastY = event.y

                // Определить зону тача, инициализировать tapCorner
                chooseMode(lastX, lastY)

                // Если собираемся перетаскивать, то нужно установить rectPivot
                // на текущую позицию rectClip и сбросить offsetH/offsetV.
                if (mode == Mode.Dragging) {
                    rectPivotReplace()
                    offsetV = 0f
                    offsetH = 0f
                }
                if (mode == Mode.Scaling.Init) {
                    minHeight = rectClip.height() / scaleRemain
                }

                // Вернуть true, если палец внутри рамки.
                mode != Mode.Waiting
            }
            MotionEvent.ACTION_MOVE -> {
                val dX = event.x - lastX
                val dY = event.y - lastY

                lastX = event.x
                lastY = event.y

                when (mode) {
                    is Mode.Dragging -> {
                        offsetV += dY
                        offsetH += dX
                        preDragging()   // Спозиционировать pathClip
                    }
                    is Mode.Scaling -> {

                        // Нужно каждый раз выбирать Scaling Mode, потому что палец
                        // может сменить направление движения на противоположное.
                        chooseScalingSubMode(event.x, event.y)

                        if (mode == Mode.Scaling.Shrink && !isScaleDownAvailable) return true
                        if (mode == Mode.Scaling.Squeeze && !isScaleUpAvailable) {
//                            logIt("Squeeze is unavailable")
                            return true
                        }

                        checkMinSizeThreshold(dX, dY)

                        // Делаем смещения одинаковыми в абс значении.
                        // Этим поддерживаем квадратную форму ViewPort'а.
//                        val d = min(abs(dX), abs(dY))
//                        offsetV = d * sign(dY)
//                        offsetH = d * sign(dX)

                        preScalingBounds()    // Изменить размер и спозиционировать pathClip
                    }
                    else -> {
                        logIt("unknown mode")
                    }
                }

                preDrawing() // Настроить pathBorder/paintStroke для рисования рамки
                invalidate()
                true
            }
            MotionEvent.ACTION_UP -> {
                // Если уменьшаем рамку, то значит увеличиваем зум
                if (rectClip.width() < rectMin.width()) {

                    if (isScaleUpAvailable) {
                        scaleController?.onScaleRequired(
                            rectClip.width() / rectClipPrev.width(),
                            calculatePivot()
                        )
                    }
                }
                true
            }
            else -> false
        }
    }

    /**
     * Вычислить pivot для скалирования. Если rectClip прижат какой-то стороной
     * к стороне rectVisible, то отталкиваемся от этой стороны. Иначе pivot'ом
     * становится центр rectClip.
     */
    private fun calculatePivot(): PointF {
        val xPivot = when {
            floor(rectClip.left).toInt() == rectVisible.left -> rectClip.left
            floor(rectClip.right).toInt() == rectVisible.right -> rectClip.right
            else -> rectClip.centerX()
        }

        val yPivot = when {
            floor(rectClip.top).toInt() == rectVisible.top -> rectClip.top
            floor(rectClip.bottom).toInt() == rectVisible.bottom -> rectClip.bottom
            else -> rectClip.centerY()
        }

        return PointF(xPivot, yPivot)
    }

    /**
     * К моменту вызова onDraw все вычисления уже выполнены.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        sourceImageBitmap?.let {
            try {
                // 1. Затененная область
                drawLayer1(canvas)
                // 2. Рамка вокруг выделенной области
                drawLayer2(canvas)
            } catch (e: Exception) {
            }
        }
    }

    /**
     * Выделенная область
     */
    private fun drawLayer1(canvas: Canvas) {
        canvas.save()
        setOuterClipping(canvas)
        canvas.drawColor(darkShadeColor)
        canvas.restore()
    }

    /**
     * Рамка вокруг выделенной области
     */
    private fun drawLayer2(canvas: Canvas) {

        canvas.save()
        setInnerClipping(canvas)
        canvas.drawPath(pathBorder, paintStroke)

        // DEBUG
        tapSquares.values.forEach {
            canvas.drawRect(it, paintTemp)
        }
        canvas.restore()
    }

    /**
     * Дистанция о места последнего TouchEvent до центра ViewPort'а (то есть до центра
     * текущего rectClip). Если эта дистанция сокращается, то мы сжимаем ViewPort,
     * иначе мы его расширяем.
     */
    private fun distanceToViewPortCenter(x: Float, y: Float): Float {
        val cX = rectClip.centerX()
        val cY = rectClip.centerY()

        return sqrt((x - cX) * (x - cX) + (y - cY) * (y - cY))
    }

    /**
     * Сформировать Path для рисования рамки.
     */
    private fun preDrawing() {
        pathBorder.apply {
            reset()
            moveTo(rectBorder.left, rectBorder.top + rectBorder.height() / 4f)
            lineTo(rectBorder.left, rectBorder.top)
            lineTo(rectBorder.left + rectBorder.width(), rectBorder.top)
            lineTo(rectBorder.left + rectBorder.width(), rectBorder.top + rectBorder.height())
            lineTo(rectBorder.left, rectBorder.top + rectBorder.height())
            close()
        }

        paintStroke.pathEffect =
            DashPathEffect(floatArrayOf(rectBorder.width() / 2f, rectBorder.width() / 2), 0f)
    }

    /**
     * Проверить, что прямоугольник @rect не выходит за границы rectVisible и если выходит,
     * то поправить offsetH/offsetV.
     */
    private fun checkBounds(rect: RectF) {

        if (rect.left + offsetH < rectVisible.left) {
            offsetH = rectVisible.left - rect.left
        } else if (rect.right + offsetH > rectVisible.right) {
            offsetH = rectVisible.right - rect.right
        }
        if (rect.top + offsetV < rectVisible.top) {
            offsetV = rectVisible.top - rect.top
        } else if (rect.bottom + offsetV > rectVisible.bottom) {
            offsetV = rectVisible.bottom - rect.bottom
        }

        val offset = min(abs(offsetH), abs(offsetV))
        offsetV = offset * sign(offsetV)
        offsetH = offset * sign(offsetH)
    }

    /**
     *
     */
    private fun checkMinSizeThreshold(dX: Float, dY: Float) {

        // Сначала вычисляем смещения без ограничений и делаем их одинаковыми в abs значении.
        // Этим поддерживаем квадратную форму ViewPort'а.
        val d = min(abs(dX), abs(dY))
        offsetV = d * sign(dY)
        offsetH = d * sign(dX)

        if (mode == Mode.Scaling.Squeeze) {

            if (rectClip.bottom + offsetV < rectClip.top + minHeight) {
                offsetV = sign(dY) * (rectClip.height() - minHeight)
                offsetH = offsetV
                isScaleUpAvailable = false
                isScaleDownAvailable = false
            }
        }
    }

    private fun preScalingBounds() {
        // Скопировать из rectClip, проверить, что не выходим за границы экрана и сдвинуть
        rectClipShifted.apply {
            set(rectClip)
            if (mode == Mode.Scaling.Shrink) {
                checkBounds(this)
            }
            offset(offsetH, offsetV)
        }

        when (mode) {
            // Палец идет от центра ViewPort'а (растягиваем)
            Mode.Scaling.Shrink -> {
                shrinkClipping()
            }
            // Палец идет к центру ViewPort'а (уменьшаем)
            Mode.Scaling.Squeeze -> {
                squeezeClipping()
            }
            else -> {
                return
            }
        }

        // После того как rectClip изменен нужно пересчитать prevDistance, потому что
        // при обработке следующего ACTION_MOVE расстояние до центра rectClip будет сравниваться
        // с расстоянием от текущего расстояния пальца.
        prevDistance = distanceToViewPortCenter(lastX, lastY)

        pathClip.apply {
            reset()
            addRoundRect(rectClip, cornerRadius, cornerRadius, Path.Direction.CW)
        }.close()

        // Также нужно обновить положение и размер rectPivot. В режиме Scaling он перемещается
        // за rectClip, чтобы потом в режиме dragging сразу иметь правильную опорную точку.
        rectPivotReplace()
    }

    /**
     * Подготовить новый pathClip для рисования.
     *
     * Здесь выполняется
     *
     */
    private fun preDragging() {
        pathClip.apply {
            reset()
            addRoundRect(rectClip, cornerRadius, cornerRadius, Path.Direction.CW)
        }.close()
    }

    /**
     * Растягиваем квадрат. Вычислить объединение.
     */
    private fun shrinkClipping() {
        rectClip.union(rectClipShifted)
    }

    /**
     * Сжимаем ViewPort. Вычислить перечечение.
     */
    private fun squeezeClipping() {
        rectClip.intersect(rectClipShifted)
    }

    /**
     * Опорный квадрат от которого будем смещать rectClip/rectBorder
     */
    private fun rectPivotInit() {

        val isVertical = rectVisible.height() >= rectVisible.width()
        val frameDimen = min(rectVisible.height(), rectVisible.width())

        rectPivot.apply {
            top = if (isVertical) {
                (rectVisible.height() - frameDimen) / 2f
            } else {
                rectVisible.top.toFloat()
            }

            bottom = top + frameDimen

            left = if (isVertical) {
                rectVisible.left.toFloat()
            } else {
                (rectVisible.width() - frameDimen) / 2f
            }
            right = left + frameDimen
        }
    }

    /**
     * Установить размер и положение rectPivot по параметрам rectClip.
     * Главное, чтобы в момент выполнения set(rectClip) в переменных offsetH/offsetV были
     * актуальные значение, потому что rectClip вычисляется делегатом и использует эти поля.
     */
    private fun rectPivotReplace() {
        val rectTmp = RectF().apply { set(rectClip) }
        rectPivot.set(rectTmp)
    }

    /**
     * Минимальный размер для viewPort. Меньше этого размера viewPort не может
     * быть на эране когда нет касаний.
     */
    private fun rectMinDelegate(): ReadWriteProperty<Any?, RectF> =
        object : ReadWriteProperty<Any?, RectF> {

            var rect = RectF()

            override fun getValue(thisRef: Any?, property: KProperty<*>): RectF {
                val dimension = min(rectVisible.width(), rectVisible.height()) / 5f * 3f
                rect.set(0f, 0f, dimension, dimension)
                return rect
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: RectF) {
                rect.set(value)
            }
        }

    /**
     * Делегат зависит от rectPivot.
     *
     * После каждого события MotionEvent.ACTION_MOVE нужно спозиционировать rectClip. Для этого
     * сначала возвращаем его в исходное состояние (не позицию rectPivot), а потом с этой позиции
     * делаем offset на offsetV/offsetH предварительно проверяя крайние условия и внося
     * корректировки в offsetV/offsetH, чтобы не вылезать за края.
     */
    private fun rectClipDelegate(): ReadWriteProperty<Any?, RectF> =
        object : ReadWriteProperty<Any?, RectF> {

            var rect = RectF()
            var prevOffsetH = 0f
            var prevOffsetV = 0f

            private fun logAndReturn(): RectF {
//                logIt("rectClip get = $rect")
                return rect
            }

            override fun getValue(thisRef: Any?, property: KProperty<*>): RectF {

                return when (mode) {
                    is Mode.Dragging, is Mode.Waiting -> {
                        rect.apply {
                            // 1. Сначала позиционируем на rectPivot
                            set(rectPivot)

                            // 2. Проверить крайние условия.
                            checkBounds()

                            // 3. Затем смещаем от pivot на вычисленные offsetH, offsetV
                            offset(offsetH, offsetV)
                        }
                        logAndReturn()
                    }
                    is Mode.Scaling, is Mode.Animating -> {
                        // Возвращаем без смещения
                        rect
                    }
                }
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: RectF) {
                rect.set(value)
            }

            private fun checkBounds() {
                if (prevOffsetH == offsetH && prevOffsetV == offsetV) return

                if (rect.left + offsetH < rectVisible.left) {
                    offsetH = rectVisible.left - rect.left
                } else if (rect.right + offsetH > rectVisible.right) {
                    offsetH = rectVisible.right - rect.right
                }
                if (rect.top + offsetV < rectVisible.top) {
                    offsetV = rectVisible.top - rect.top
                } else if (rect.bottom + offsetV > rectVisible.bottom) {
                    offsetV = rectVisible.bottom - rect.bottom
                }

                prevOffsetH = offsetH
                prevOffsetV = offsetV
            }
        }

    /**
     * Делегат зависит от rectClip
     */
    private fun rectBorderDelegate(): ReadWriteProperty<Any?, RectF> =
        object : ReadWriteProperty<Any?, RectF> {

            var rect = RectF()
            var rectTmp = RectF()

            override fun getValue(thisRef: Any?, property: KProperty<*>): RectF {

                rectTmp.set(rectClip)

                rect.apply {
                    left = rectTmp.left + borderWidth / 2f
                    top = rectTmp.top + borderWidth / 2f
                    right = rectTmp.right - borderWidth / 2f
                    bottom = rectTmp.bottom - borderWidth / 2f
                }
                return rect
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: RectF) {}
        }

    /**
     * Делегат зависит от rectClip. Области для масштабирования рамки
     */
    private fun rectTapDelegate(): ReadWriteProperty<Any?, Map<TapArea, RectF>> =
        object : ReadWriteProperty<Any?, Map<TapArea, RectF>> {

            val ratio = 0.25f
            val square = RectF()
            val squares = mapOf(
                lt to RectF(),
                rt to RectF(),
                rb to RectF(),
                lb to RectF()
            )
            val rectTmp = RectF()

            override fun getValue(thisRef: Any?, property: KProperty<*>): Map<TapArea, RectF> {

                rectTmp.set(rectClip)

                square.apply {
                    set(rectTmp)
                    scale(ratio, ratio)
                }

                squares.entries.forEach { e ->
                    when (e.key) {
                        lt -> {
                            e.value.set(square)
                        }
                        rb -> {
                            e.value.apply {
                                set(square)
                                offset(
                                    rectTmp.width() - square.width(),
                                    rectTmp.height() - square.height()
                                )
                            }
                        }
                        rt -> {
                            e.value.apply {
                                set(square)
                                offset(rectTmp.width() - square.width(), 0f)
                            }
                        }
                        lb -> {
                            e.value.apply {
                                set(square)
                                offset(0f, rectTmp.height() - square.height())
                            }
                        }
                    }
                }

                return squares
            }

            override fun setValue(
                thisRef: Any?,
                property: KProperty<*>,
                value: Map<TapArea, RectF>
            ) {
            }
        }

    private fun setOuterClipping(canvas: Canvas) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            canvas.clipOutPath(pathClip)
        } else {
            canvas.clipPath(pathClip, Region.Op.DIFFERENCE)
        }
    }

    private fun setInnerClipping(canvas: Canvas) {
        canvas.clipPath(pathClip)
    }

    /**
     * При изменении размера экрана все сбрасываем в исходное состояние
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        resetState()
        rectPivotInit()
        preDragging()
        preDrawing()
        invalidate()
    }

    /**
     * Выбрать режим в зависимости от позиции касания.
     * Если ткнули в квадраты по краям viewport'а, то масштабируем, иначе передвигаем.
     */
    private fun chooseMode(x: Float, y: Float) {

        tapSquares.values.forEach { rect ->
            if (rect.contains(x, y)) {
                mode = Mode.Scaling.Init
                return
            }
        }

        mode = if (rectClip.contains(x, y)) {
            Mode.Dragging
        } else {
            Mode.Waiting
        }
    }

    /**
     * Выбрать режим скалирования: растяжение/сжатие (Shrink/Squeeze)
     */
    private fun chooseScalingSubMode(x: Float, y: Float) {
        val dist = distanceToViewPortCenter(x, y)
        mode = if (dist < prevDistance) Mode.Scaling.Squeeze else Mode.Scaling.Shrink
    }

    /**
     * Сбросить состояние в исходное
     */
    private fun resetState() {
        offsetH = 0f
        offsetV = 0f
        lastX = 0f
        lastX = 0f
        mode = Mode.Waiting
    }

    // DEBUG
    private val paintTemp = Paint().apply {
        color = Color.argb(0xff, 0xff, 0xff, 0xff)
        style = Paint.Style.STROKE
        strokeWidth = 1.2f
    }

    private var pivot: PointF? = null

    override fun onPreScale(factor: Float, pivot: PointF) {
        if (!isScaleDownAvailable) return

        if (rectClip.width() >= rectMin.width()) return

        scaleFrom = rectClip.width()
        scaleTo = rectMin.width()
        this.pivot = rectClip.center
    }

    override fun onScale(fraction: Float) {
        if (!isScaleDownAvailable) return

        requireNotNull(pivot)

        val rectDim = evaluator.evaluate(fraction, scaleFrom, scaleTo)

        // Установить rect по центру относительно pivot'a
        rectTemp.apply {
            left = pivot!!.x - rectDim / 2f
            top = pivot!!.y - rectDim / 2f
            right = left + rectDim
            bottom = top + rectDim
        }

        // Проверить крайние условия по X
        val offsetX = when {
            rectTemp.left < rectVisible.left -> rectVisible.left - rectTemp.left
            rectTemp.right > rectVisible.right -> rectVisible.right - rectTemp.right
            else -> 0f
        }

        // Проверить крайние условия по Y
        val offsetY = when {
            rectTemp.top < rectVisible.top -> rectVisible.top - rectTemp.top
            rectTemp.bottom > rectVisible.bottom -> rectVisible.bottom - rectTemp.bottom
            else -> 0f
        }

        rectTemp.offset(offsetX, offsetY)
        rectClip.set(rectTemp)
        preScalingBounds()
        preDrawing()
    }

    /**
     * Анимированно восстановить размер viwPort'a
     */
    private fun restoreAnimated() {

        if (rectClip.width() >= rectMin.width()) return

        ValueAnimator.ofFloat(rectClip.width(), rectMin.width()).apply {
            duration = animDuration

            val pivot = rectClip.center

            addUpdateListener { animator ->
                val rectDim = animator.animatedValue as Float

                // Установить rect по центру относительно pivot'a
                rectTemp.apply {
                    left = pivot.x - rectDim / 2f
                    top = pivot.y - rectDim / 2f
                    right = left + rectDim
                    bottom = top + rectDim
                }

                // Проверить крайние условия по X
                val offsetX = when {
                    rectTemp.left < rectVisible.left -> rectVisible.left - rectTemp.left
                    rectTemp.right > rectVisible.right -> rectVisible.right - rectTemp.right
                    else -> 0f
                }

                // Проверить крайние условия по Y
                val offsetY = when {
                    rectTemp.top < rectVisible.top -> rectVisible.top - rectTemp.top
                    rectTemp.bottom > rectVisible.bottom -> rectVisible.bottom - rectTemp.bottom
                    else -> 0f
                }

                rectTemp.offset(offsetX, offsetY)
                rectClip.set(rectTemp)
                preScalingBounds()
                preDrawing()
                invalidate()
            }
        }.start()
    }
}