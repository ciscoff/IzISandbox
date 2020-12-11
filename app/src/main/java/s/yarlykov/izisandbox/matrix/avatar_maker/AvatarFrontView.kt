package s.yarlykov.izisandbox.matrix.avatar_maker

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.dsl.extenstions.dp_f
import s.yarlykov.izisandbox.extensions.scale
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


/**
 * В общем проблема в том, что при полноэкранном режиме получается слишком большая битмапа
 * (даже для квадратика clip) и из-за этого подлагивает. Прорисовка кадра занимает больше 16.67ms.
 * Padding в родительском контейнере отчасти решил проблему, но мне кажется, что можно пойти
 * дальше. Например двигаем рамку вверх. Тогда у старого и нового прямоугольника остается общая
 * часть (закрашена точками) и её не нужно перерисовывать. Нужно только добавить немного сверху и
 * удалить немного снизу. Нужно подумать как это сделать. Кстати SurfaceView не затирается
 * на каждой итерации.
 *
 * NOTE: Наверное не прокатит, потому что метод invalidate(Rect) больше не работает !
 */

//   _________________
//   |               |
//  _|_______________|_   /\
//  ||. . . . . . . .||   ||  Move UP
//  || . . . . . . . ||
//  ||. . . . . . . .||
//  || . . . . . . . ||
//  ||_______________||
//  |                 |
//  |_________________|
//

class AvatarFrontView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AvatarBaseView(context, attrs, defStyleAttr) {

    /**
     * Квадратные области для масштабирования рамки видоискателя
     */
    private val tapSquares = mapOf(
        lt to RectF(),
        rt to RectF(),
        rb to RectF(),
        lb to RectF()
    )

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
     * Paint (color Yellow)
     */
    private val paintStroke: Paint = Paint().apply {
        color = Color.argb(0xff, 0xff, 0xff, 0x0)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = borderWidth
        isAntiAlias = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        return when (event.action) {
            // Вернуть true, если палец внутри рамки.
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
                rectClip.contains(lastX, lastY)
            }
            MotionEvent.ACTION_MOVE -> {
                val dragX = event.x
                val dragY = event.y

                val dX = dragX - lastX
                val dY = dragY - lastY

                offsetV += dY
                offsetH += dX

                lastX = dragX
                lastY = dragY

                invalidate()
                true
            }
            MotionEvent.ACTION_UP -> {
                true
            }
            else -> false
        }
    }

    // DEBUG
    private val paintTemp = Paint().apply {
        color = Color.argb(0xff, 0xff, 0xff, 0xff)
        style = Paint.Style.STROKE
        strokeWidth = 1.2f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        sourceImageBitmap?.let {
            pathClip.apply {
                reset()
                addRoundRect(rectClip, cornerRadius, cornerRadius, Path.Direction.CW)
            }.close()

            canvas.save()
            canvas.clipPath(pathClip)
            // 1. Выделенная область
            drawLayer2(canvas, it)
            // 2. Рамка вокруг выделенной области
            drawLayer3(canvas)
            canvas.restore()
        }

        // DEBUG
        tapSquares.values.forEach {
            canvas.drawRect(it, paintTemp)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rectPivotUpdate()
        tapSquaresUpdate()
        invalidate()
    }

    /**
     * Выделенная область
     */
    private fun drawLayer2(canvas: Canvas, bitmap: Bitmap) {
        canvas.drawBitmap(bitmap, rectSourceImage, rectDest, null)
    }

    /**
     * Рамка вокруг выделенной области
     */
    private fun drawLayer3(canvas: Canvas) {

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
     * Области для масштабирования рамки
     */
    private fun tapSquaresUpdate() {

        val ratio = 0.25f
        val square = RectF(rectPivot).apply { scale(ratio, ratio) }

        tapSquares.entries.forEach { e ->
            when (e.key) {
                lt -> {
                    e.value.set(square)
                }
                rb -> {
                    e.value.apply {
                        set(square)
                        offset(
                            rectPivot.width() - square.width(),
                            rectPivot.height() - square.height()
                        )
                    }
                }
                rt -> {
                    e.value.apply {
                        set(square)
                        offset(rectPivot.width() - square.width(), 0f)
                    }
                }
                lb -> {
                    e.value.apply {
                        set(square)
                        offset(0f, rectPivot.height() - square.height())
                    }
                }
            }
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

            override fun getValue(thisRef: Any?, property: KProperty<*>): RectF {
                return rect.apply {
                    set(rectPivot)

                    if (abs(offsetV) > offsetMaxV) {
                        offsetV = offsetMaxV * sign(offsetV)
                    }

                    if (abs(offsetH) > offsetMaxH) {
                        offsetH = offsetMaxH * sign(offsetH)
                    }

                    offset(offsetH, offsetV)
                }
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: RectF) {}
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
}