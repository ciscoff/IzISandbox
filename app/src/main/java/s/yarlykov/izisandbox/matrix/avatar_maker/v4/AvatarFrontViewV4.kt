package s.yarlykov.izisandbox.matrix.avatar_maker.v4

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.scale
import s.yarlykov.izisandbox.matrix.avatar_maker.gesture.*
import s.yarlykov.izisandbox.matrix.avatar_maker.v3.AvatarBaseViewV3
import s.yarlykov.izisandbox.utils.logIt
import kotlin.math.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class AvatarFrontViewV4 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AvatarBaseViewV3(context, attrs, defStyleAttr) {

    private var mode: Mode = Mode.Waiting

    /**
     * @rectClip - квадратная область для рисования рамки выбора части изображения под аватарку.
     * Это квадрат в координатах канвы/view.
     */
    private var rectClip: RectF by rectClipDelegate()

    /**
     * @rectPivot - квадрат в координатах канвы, который играет роль хранлилища предыдущего
     * положения rectClip в операции Dragging: он помнит где был rectClip в момент предыдущего
     * ACTION_DOWN и при очередном ACTION_MOVE rectClip сначала позиционируется на rectPivot,
     * а потом смещается в новое положение.
     */
    private val rectPivot = RectF()

    /**
     * @rectClipShifted служит для временного копирования rectClip в операциях скалирования
     */
    private val rectClipShifted = RectF()

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
     * Paint (color Yellow)
     */
    private val paintStroke: Paint = Paint().apply {
        color = Color.argb(0xff, 0xff, 0xff, 0x0)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = borderWidth
        isAntiAlias = true
    }

    // DEBUG
    private val paintTemp = Paint().apply {
        color = Color.argb(0xff, 0xff, 0xff, 0xff)
        style = Paint.Style.STROKE
        strokeWidth = 1.2f
    }

    /**
     * Темный полупрозрачный цвет
     */
    private val darkShadeColor = Color.argb(0x88, 0x00, 0x00, 0x00)


    /**
     * Ширина линии рамки
     */
    private val borderWidth = context.resources.getDimension(R.dimen.view_port_border_stroke_width)

    /**
     * Радиус закругления рамки видоискателя
     */
    private val cornerRadius =
        context.resources.getDimension(R.dimen.view_port_border_corner_radius)

    /**
     * Квадратные области для масштабирования рамки.
     */
    private val tapSquares: Map<TapArea, RectF> by rectTapDelegate()

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

    override fun onTouchEvent(event: MotionEvent): Boolean {

        return when (event.action) {

            MotionEvent.ACTION_DOWN -> {

                // Определить режим жеста и создать Gesture (для скалинга)
                detectMode(event.x, event.y)

                lastX = event.x
                lastY = event.y

                // Если собираемся перетаскивать, то нужно установить rectPivot
                // на текущую позицию rectClip и сбросить offsetH/offsetV.
                // NOTE: Если собираемся скалировать, то offsetV/offsetH не изменяем,
                // так как по ним выставлен rectClip.
                if (mode == Mode.Dragging) {
                    rectPivotMove()
                    offsetV = 0f
                    offsetH = 0f
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
                        // Спозиционировать rectClip/pathClip. rectClip автоматически
                        // выравнивается, чтобы не выходить за границы.
                        preDragging()
                    }
                    is Mode.Scaling -> {
                        mode = gesture.detectScalingSubMode(event.x)

                        if (mode == Mode.Scaling.Squeeze) {
                            val offset = gesture.confirmSqueezeOffset(dX)
                            offsetH = offset.x
                            offsetV = offset.y
                            logIt("dX=$dX, dY=$dY, offsetH=$offsetH, offsetV=$offsetV")
                        }

                        preScalingBounds()
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
                true
            }
            else -> false
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        resetState()

        rectPivotInit()
        preDragging()
        preDrawing()
        invalidate()


    }

    /**
     * --------------------------------------------------------------------------------------
     * Позиционирование / Скалирование
     * --------------------------------------------------------------------------------------
     */

    private lateinit var gesture: Gesture

    override fun onPreScale(factor: Float, pivot: PointF) {
        // TODO
    }

    override fun onScale(fraction: Float) {
        // TODO
    }

    /**
     * Выбрать режим в зависимости от позиции касания.
     * Если ткнули в квадраты по краям viewport'а, то масштабируем, иначе передвигаем.
     */
    private fun detectMode(x: Float, y: Float) {

        tapSquares.entries.forEach { entry ->

            val (area, rect) = entry
            if (rect.contains(x, y)) {

                val (cornerX, cornerY) = when (area) {
                    is lt -> rectClip.left to rectClip.top
                    is lb -> rectClip.left to rectClip.bottom
                    is rt -> rectClip.right to rectClip.top
                    is rb -> rectClip.right to rectClip.bottom
                }

                gesture = Gesture(
                    TapCorner(area, PointF(x, y), PointF(cornerX, cornerY)),
                    rectClip.width() - rectClip.width() / scaleRemain
                )

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

        pathClip.apply {
            reset()
            addRoundRect(rectClip, cornerRadius, cornerRadius, Path.Direction.CW)
        }.close()

        // Также нужно обновить положение и размер rectPivot. В режиме Scaling он перемещается
        // за rectClip, чтобы потом в режиме dragging сразу иметь правильную опорную точку.
        rectPivotMove()
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
     * Установить размер и положение rectPivot по параметрам rectClip.
     * Главное, чтобы в момент выполнения set(rectClip) в переменных offsetH/offsetV были
     * актуальные значение, потому что rectClip вычисляется делегатом и использует эти поля.
     */
    private fun rectPivotMove() {
//        val rectTmp = RectF().apply { set(rectClip) }
        rectPivot.set(rectClip)
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
     * Pivot для скалирования. Если rectClip прижат какой-то стороной к стороне rectVisible,
     * то отталкиваемся от этой стороны. Иначе pivot'ом становится центр rectClip.
     */
    private val scalePivot: PointF
        get() {
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
     * --------------------------------------------------------------------------------------
     * Рисование
     * --------------------------------------------------------------------------------------
     */

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
     * Подготовить новый pathClip для рисования
     */
    private fun preDragging() {
        pathClip.apply {
            reset()
            addRoundRect(rectClip, cornerRadius, cornerRadius, Path.Direction.CW)
        }.close()
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
     * --------------------------------------------------------------------------------------
     * Делегаты
     * --------------------------------------------------------------------------------------
     */

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

            // DEBUG
            private fun logAndReturn(): RectF {
//                logIt("rectClip=$rect, rectPivot=$rectPivot, offsetH=$offsetH, offsetV=$offsetV")
                return rect
            }

            override fun getValue(thisRef: Any?, property: KProperty<*>): RectF {

                return when (mode) {
                    is Mode.Dragging, is Mode.Waiting -> {
                        rect.apply {
                            // 1. Сначала позиционируем на rectPivot
                            set(rectPivot)

                            // 2. Проверить крайние условия.
                            if (prevOffsetH != offsetH || prevOffsetV != offsetV) {
                                checkBounds(this)
                                prevOffsetH = offsetH
                                prevOffsetV = offsetV
                            }

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
     * --------------------------------------------------------------------------------------
     * Утилитные функции
     * --------------------------------------------------------------------------------------
     */

    /**
     * Проверить, что прямоугольник @rect не выходит за границы rectVisible.
     * И если выходит, то поправить offsetH/offsetV.
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

//        val offset = min(abs(offsetH), abs(offsetV))
//        offsetV = offset * sign(offsetV)
//        offsetH = offset * sign(offsetH)
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
}