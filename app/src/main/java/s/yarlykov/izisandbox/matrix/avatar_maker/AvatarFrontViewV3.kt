package s.yarlykov.izisandbox.matrix.avatar_maker

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import s.yarlykov.izisandbox.dsl.extenstions.dp_f
import s.yarlykov.izisandbox.extensions.scale
import s.yarlykov.izisandbox.utils.logIt
import kotlin.math.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Видимо понадобятся:
 * ScaleGestureDetector.OnScaleGestureListener
 * GestureDetector.OnGestureListener
 */

class AvatarFrontViewV3 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AvatarBaseView(context, attrs, defStyleAttr) {

    private var mode: Mode = Mode.Unknown

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
     * @rectClip область (квадратная) для рисования рамки выбора части изображения под аватарку.
     */
    private val rectClip: RectF by rectClipDelegate()

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
     * @offsetV - это дополнительный вертикальный offset учитывающий жест перетаскивания
     * @offsetH - это дополнительный горизонтальный offset учитывающий жест перетаскивания
     */
    private var offsetV = 0f
    private var offsetH = 0f

    private var offsetMaxV = 0f
    private var offsetMaxH = 0f

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

    private var prevDistance: Float = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {

        return when (event.action) {
            // Вернуть true, если палец внутри рамки.
            MotionEvent.ACTION_DOWN -> {
                prevDistance = distanceToViewPortCenter(event.x, event.y)

                offsetV = 0f
                offsetH = 0f
                lastX = event.x
                lastY = event.y
                chooseMode(lastX, lastY)
                logIt(
                    "ACTION_DOWN prevDistance=$prevDistance, mode=${mode::class.java.simpleName}",
                    true,
                    "PLPL"
                )
                mode != Mode.Unknown
            }
            MotionEvent.ACTION_MOVE -> {
                val dX = event.x - lastX
                val dY = event.y - lastY

                logIt(
                    "ACTION_MOVE new_dist=${
                        distanceToViewPortCenter(
                            event.x,
                            event.y
                        )
                    }, mode=${mode::class.java.simpleName}", true, "PLPL"
                )

                when (mode) {
                    is Mode.Dragging -> {
                        offsetV += dY
                        offsetH += dX
                    }
                    is Mode.Scaling -> {

                        // Нужно каждый раз выбирать Scaling Mode, потому что палец
                        // может сменить направление движения на противоположное.
                        chooseScalingSubMode(event.x, event.y)

                        // Делаем смещения одинаковыми в абс значении.
                        // Этим поддерживаем квадратную форму ViewPort'а.
                        val d = min(abs(dX), abs(dY))
                        offsetV = d * sign(dY)
                        offsetH = d * sign(dX)
                    }
                    else -> {
                        logIt("unknown mode")
                    }
                }

                lastX = event.x
                lastY = event.y

                invalidate()
                true
            }
            MotionEvent.ACTION_UP -> {
                mode = Mode.Unknown
                offsetV = 0f
                offsetH = 0f
                true
            }
            else -> false
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        logIt(
            "onDraw Mode=${mode::class.java.simpleName}, mode=${mode::class.java.simpleName}",
            true,
            "PLPL"
        )

        sourceImageBitmap?.let {
//            pathClip.apply {
//                reset()
//                addRoundRect(rectClip, cornerRadius, cornerRadius, Path.Direction.CW)
//            }.close()

            when (mode) {
                is Mode.Dragging, is Mode.Unknown -> {
                    drawDragging(canvas)
                }
                is Mode.Scaling -> {
                    drawScaling(canvas)
                }
            }
        }
    }


    /**
     * Дистанция о места последнего TouchEvent до центра ViewPort'а (то есть до центра
     * текущего rectClip). Если эта дистанция сокращается, то мы сжимаем ViewPort,
     * иначе мы его расширяем.
     */
    private fun distanceToViewPortCenter(x: Float, y: Float): Float {
        val cX = rectClip.centerX()/*left + rectClip.width() / 2*/
        val cY = rectClip.centerY()/*top + rectClip.height() / 2*/

        return sqrt((x - cX) * (x - cX) + (y - cY) * (y - cY))
    }

    private fun drawDragging(canvas: Canvas) {

        pathClip.apply {
            reset()
            addRoundRect(rectClip, cornerRadius, cornerRadius, Path.Direction.CW)
        }.close()

        // 1. Затененная область
        drawLayer2(canvas)

        // 2. Рамка вокруг выделенной области
        drawLayer3(canvas)
    }


    private fun drawScaling(canvas: Canvas) {

        // Скопировать из rectClip и сдвинуть
        rectClipShifted.apply {
            set(rectClip)
            offset(offsetH, offsetV)
        }

        when (mode) {
            // Палец идет к центру ViewPort'а
            Mode.Scaling.Shrink -> {
                shrinkClipping()
            }
            // Палец идет от центра ViewPort'а
            Mode.Scaling.Squeeze -> {
                squeezeClipping()
            }
            else -> {
            }
        }

        // После того как rectClip изменен нужно пересчитать prevDistance потому что
        // при обработке следующего ACTION_MOVE расстояние до центра rectClip будет сравниваться
        // с расстоянием от текущего расстояния пальца.
        prevDistance = distanceToViewPortCenter(lastX, lastY)

        pathClip.apply {
            reset()
            addRoundRect(rectClip, cornerRadius, cornerRadius, Path.Direction.CW)
        }.close()

        // 1. Затененная область
        drawLayer2(canvas)

        // 2. Рамка вокруг выделенной области
        drawLayer3(canvas)
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
     * Выделенная область
     */
    private fun drawLayer2(canvas: Canvas) {
        try {
            canvas.save()
            setOuterClipping(canvas)
            canvas.drawColor(darkShadeColor)
            canvas.restore()
        } catch (e: Exception) {
        }
    }

    /**
     * Рамка вокруг выделенной области
     */
    private fun drawLayer3(canvas: Canvas) {

        try {
            canvas.save()
            setInnerClipping(canvas)
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
            canvas.drawPath(pathBorder, paintStroke)

            // DEBUG
            tapSquares.values.forEach {
                canvas.drawRect(it, paintTemp)
            }

            canvas.restore()
        } catch (e: Exception) {
        }
    }

    /**
     * Опорный квадрат от которого будем смещать rectClip/rectBorder
     */
    private fun rectPivotUpdate() {

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

        offsetMaxV = rectVisible.bottom - rectPivot.bottom
        offsetMaxH = rectVisible.right - rectPivot.right
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

            override fun getValue(thisRef: Any?, property: KProperty<*>): RectF {

                return when (mode) {
                    is Mode.Dragging, is Mode.Unknown -> {
                        rect.apply {
                            set(rectPivot)

                            if (abs(offsetV) > offsetMaxV) {
                                offsetV = offsetMaxV * sign(offsetV)
                            }

                            if (abs(offsetH) > offsetMaxH) {
                                offsetH = offsetMaxH * sign(offsetH)
                            }

                            // Смещаем прямоугольник
                            // TODO Это ХУЕВО ! При каждом чтении смещаем !! Хуево
                            offset(offsetH, offsetV)
                        }
                    }
                    is Mode.Scaling -> {
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
     * Делегат зависит от rectClip
     */
    private fun rectBorderDelegate(): ReadWriteProperty<Any?, RectF> =
        object : ReadWriteProperty<Any?, RectF> {

            var rect = RectF()

            override fun getValue(thisRef: Any?, property: KProperty<*>): RectF {

                rect.apply {
                    left = rectClip.left + borderWidth / 2f
                    top = rectClip.top + borderWidth / 2f
                    right = rectClip.right - borderWidth / 2f
                    bottom = rectClip.bottom - borderWidth / 2f
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

            override fun getValue(thisRef: Any?, property: KProperty<*>): Map<TapArea, RectF> {

                square.apply {
                    set(rectClip)
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
                                    rectClip.width() - square.width(),
                                    rectClip.height() - square.height()
                                )
                            }
                        }
                        rt -> {
                            e.value.apply {
                                set(square)
                                offset(rectClip.width() - square.width(), 0f)
                            }
                        }
                        lb -> {
                            e.value.apply {
                                set(square)
                                offset(0f, rectClip.height() - square.height())
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rectPivotUpdate()
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

        mode = if (rectClip.contains(x, y)) Mode.Dragging else Mode.Unknown
    }

    private fun chooseScalingSubMode(x: Float, y: Float) {
        val dist = distanceToViewPortCenter(x, y)
        mode = if (dist < prevDistance) Mode.Scaling.Squeeze else Mode.Scaling.Shrink
    }

    // DEBUG
    private val paintTemp = Paint().apply {
        color = Color.argb(0xff, 0xff, 0xff, 0xff)
        style = Paint.Style.STROKE
        strokeWidth = 1.2f
    }
}