package s.yarlykov.izisandbox.matrix.avatar_maker

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import s.yarlykov.izisandbox.R
import kotlin.math.min
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class AvatarPreView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

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
     * @pathClip определяется через @rectClip.
     */
    private val pathClip = Path()

    /**
     * Цветовые фильтры поярче/потемнее.
     */
    private val colorFilterLighten = LightingColorFilter(0xFFFFFFFF.toInt(), 0x00222222)
    private val colorFilterDarken = LightingColorFilter(0xFF7F7F7F.toInt(), 0x00000000)

    /**
     * Paint для заливки фона исходной Bitmap'ой с применением цветового фильтра.
     */
    private val paintBackground = Paint(Color.GRAY).apply { colorFilter = colorFilterDarken }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        sourceImageBitmap =
            BitmapFactory.decodeResource(context.resources, R.drawable.nature)?.also {
                rectSourceImage = Rect(0, 0, it.width, it.height)
            }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        sourceImageBitmap?.let {
            canvas.drawBitmap(it, rectSourceImage, rectDest, paintBackground)
        }

        sourceImageBitmap?.let {
            pathClip.apply {
                reset()
                addRect(rectClip, Path.Direction.CW)
            }.close()

            canvas.save()
            canvas.clipPath(pathClip)
            canvas.drawBitmap(it, rectSourceImage, rectDest, null)
            canvas.restore()
        }
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
                val ratio = height.toFloat() / rectSourceImage.height()
                val scaledWidth = (rectSourceImage.width() * ratio).toInt()

                return rect.apply {
                    top = 0
                    bottom = this@AvatarPreView.height
                    left = (this@AvatarPreView.width - scaledWidth) / 2
                    right = left + scaledWidth
                }
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
                return rect.apply {
                    top = 0
                    bottom = this@AvatarPreView.height
                    left =
                        if (rectDest.left <= 0) 0 else rectDest.left
                    right =
                        if (rectDest.right >= this@AvatarPreView.width) this@AvatarPreView.width else rectDest.right
                }
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: Rect) {}
        }

    /**
     * Делегат зависит от rectVisible
     */
    private fun rectClipDelegate(): ReadWriteProperty<Any?, RectF> =
        object : ReadWriteProperty<Any?, RectF> {

            var rect = RectF()

            override fun getValue(thisRef: Any?, property: KProperty<*>): RectF {

                val isVertical = rectVisible.height() >= rectVisible.width()
                val frameDimen = min(rectVisible.height(), rectVisible.width())

                return rect.apply {
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

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: RectF) {}
        }
}