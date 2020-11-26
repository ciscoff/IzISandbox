package s.yarlykov.izisandbox.matrix.avatar_maker

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.contains
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.dsl.extenstions.dp_f
import s.yarlykov.izisandbox.utils.logIt
import kotlin.math.min
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class AvatarPreView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /**
     * Ширина линии рамки
     */
    private val borderWidth = dp_f(2f)

    /**
     * Изменился размер View. Потребуется пересчет всех компонентов.
     */
    private var sizeChanged = false

    private var clipChanged = false

    /**
     * Раидиус закругления рамки
     */
    private val cornerRadius = dp_f(2f)

    /**
     * Исходная Bitmap и её размеры
     */
    private var sourceImageBitmap: Bitmap? = null
    private var rectSourceImage = Rect()

    /**
     * @rectDest прямоугольник (в координатах канвы) куда будем рисовать исходную Bitmap.
     * Он может выходить краями за пределя экрана.
     */
    private val rectDest: Rect by rectDestDelegate()

    /**
     * @rectVisible прямоугольник (в координатах канвы) определяющий видимую часть
     * картинки. Все что не влезло в экран не учитвыается.
     */
    private val rectVisible: Rect by rectVisibleDelegate()

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

    /**
     * Цветовые фильтры поярче/потемнее.
     */
    private val colorFilterLighten = LightingColorFilter(0xFFFFFFFF.toInt(), 0x00222222)
    private val colorFilterDarken = LightingColorFilter(0xFF7F7F7F.toInt(), 0x00000000)

    /**
     * Paint для заливки фона исходной Bitmap'ой с применением цветового фильтра.
     */
    private val paintBackground = Paint(Color.GRAY).apply { colorFilter = colorFilterDarken }

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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        sourceImageBitmap =
            BitmapFactory.decodeResource(context.resources, R.drawable.nature)?.also {
                rectSourceImage = Rect(0, 0, it.width, it.height)
            }
    }

    private var lastX = 0f
    private var lastY = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {

        return when (event.action) {
            // Вернуть true если палец внутри рамки
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

                moveIfPossible()

//                clipChanged = true
//                invalidate()
                true

            }
            MotionEvent.ACTION_UP -> {
                true

            }
            else -> false
        }
    }

    private fun moveIfPossible() {
        val rectC = RectF(rectClip).apply { offset(0f, offsetV) }
        val rectV = RectF(rectVisible)

        val offBefore = offsetV

        if(!rectV.contains(rectC)) {
            offsetV += if(rectC.top < rectV.top) rectV.top - rectC.top else rectC.bottom - rectV.bottom
        }

        logIt("rectC=$rectC, rectV=$rectV, offsetV_before=$offBefore, offsetV_after=$offsetV contains=${rectV.contains(rectC)}", "PLPL")

        clipChanged = true
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Background
        drawLayer1(canvas)

        sourceImageBitmap?.let {
            pathClip.apply {
                reset()
                addRoundRect(rectClip, cornerRadius, cornerRadius, Path.Direction.CW)
            }.close()

            canvas.save()
            canvas.clipPath(pathClip)
            // Выделенная область
            drawLayer2(canvas, it)
            // Рамка вокруг выделенной области
            drawLayer3(canvas)
            canvas.restore()

            sizeChanged = false
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        sizeChanged = true
    }

    /**
     * Background
     */
    private fun drawLayer1(canvas: Canvas) {
        sourceImageBitmap?.let {
            canvas.drawBitmap(it, rectSourceImage, rectDest, paintBackground)
        }
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
     * Растянуть по высоте с ratio.
     *
     * При любой ориентации "натягиваем" битмапу по высоте. При этом, часть битмапы
     * может оказаться за пределами боковых границ view. Но это фигня. Главное, что
     * нет искажений от растяжки/сжатия.
     *
     * Делегат зависит от rectSourceImage
     */
    private fun rectDestDelegate(): ReadWriteProperty<Any?, Rect> =
        object : ReadWriteProperty<Any?, Rect> {

            var rect = Rect()

            override fun getValue(thisRef: Any?, property: KProperty<*>): Rect {

                if (rect.isEmpty || sizeChanged) {

                    val ratio = height.toFloat() / rectSourceImage.height()
                    val scaledWidth = (rectSourceImage.width() * ratio).toInt()

                    rect.apply {
                        top = 0
                        bottom = this@AvatarPreView.height
                        left = ((this@AvatarPreView.width - scaledWidth) / 2f).toInt()
                        right = left + scaledWidth
                    }
                }
                return rect
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: Rect) {}
        }

    /**
     * Делегат зависит от rectDest
     */
    private fun rectVisibleDelegate(): ReadWriteProperty<Any?, Rect> =
        object : ReadWriteProperty<Any?, Rect> {

            var rect = Rect()

            override fun getValue(thisRef: Any?, property: KProperty<*>): Rect {

                if (rect.isEmpty || sizeChanged) {
                    rect.apply {
                        top = 0
                        bottom = this@AvatarPreView.height
                        left =
                            if (rectDest.left <= 0) 0 else rectDest.left
                        right =
                            if (rectDest.right >= this@AvatarPreView.width) this@AvatarPreView.width else rectDest.right
                    }
                }

                return rect
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: Rect) {}
        }


    private var offsetMaxV = 0f


    /**
     * Делегат зависит от rectVisible
     */
    private fun rectClipDelegate(): ReadWriteProperty<Any?, RectF> =
        object : ReadWriteProperty<Any?, RectF> {

            // Опорный Rect установленный по центру rectVisible
            var rect = RectF()

            override fun getValue(thisRef: Any?, property: KProperty<*>): RectF {

                return when {
                    rect.isEmpty || sizeChanged -> {
                        val isVertical = rectVisible.height() >= rectVisible.width()
                        val frameDimen = min(rectVisible.height(), rectVisible.width())

                        rect.apply {
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
                    clipChanged -> {
                        val rect2 = RectF(rect).apply { offset(0f, offsetV) }

                        rect2
                    }

                    else -> rect
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

                if (rect.isEmpty || sizeChanged || clipChanged) {

                    rect.apply {
                        left = rectClip.left + borderWidth / 2f
                        top = rectClip.top + borderWidth / 2f
                        right = rectClip.right - borderWidth / 2f
                        bottom = rectClip.bottom - borderWidth / 2f
                    }
                }

                return rect
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: RectF) {}
        }
}