package s.yarlykov.izisandbox.matrix.avatar_maker.v3

import android.content.Context
import android.graphics.Bitmap
import android.graphics.LightingColorFilter
import android.graphics.PointF
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.matrix.avatar_maker.MediaDataConsumer

abstract class AvatarBaseViewV3 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), MediaDataConsumer {

    /**
     * Цветовые фильтры поярче/потемнее.
     */
    protected val colorFilterLighten = LightingColorFilter(0xFFFFFFFF.toInt(), 0x00222222)
    protected val colorFilterDarken = LightingColorFilter(0xFF7F7F7F.toInt(), 0x00000000)

    protected val animDuration = context.resources.getInteger(R.integer.anim_duration_avatar).toLong()

    /**
     * @rectDest прямоугольник (в координатах канвы. у канвы left/top = 0/0) куда будем
     * "проецировать" исходную Bitmap. Он может выходить краями за пределы видимой области канвы
     * и иметь отрицательные left/top.
     */
    protected val rectDest = Rect()

    /**
     * @rectVisible прямоугольник (в координатах канвы. у канвы left/top = 0/0) определяющий
     * видимую часть картинки. Это как бы смотровая щель - все что не влезло в экран не учитывается.
     */
    protected val rectVisible = Rect()

    /**
     * Исходная Bitmap и её размеры
     *
     * sourceImageBitmap - битмапа
     * rectSourceImage - прямоугольник видимой части битмапы в координатах БИТМАПЫ
     */
    protected var sourceImageBitmap: Bitmap? = null
    protected var rectBitmapVisible = Rect()

    /**
     * Для AvatarBackView достаточно этих операцию + invalidate.
     * Для AvatarFrontView нужны дополнительные вычисления + invalidate.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rectDestUpdate()
        rectVisibleUpdate()
    }

    /**
     * Растянуть по высоте с ratio.
     *
     * При любой ориентации "натягиваем" битмапу по высоте. При этом, часть битмапы
     * может оказаться за пределами боковых границ view. Но это фигня. Главное, что
     * нет искажений от растяжки/сжатия.
     */
    private fun rectDestUpdate() {

        val ratio = height.toFloat() / rectBitmapVisible.height()
        val scaledWidth = (rectBitmapVisible.width() * ratio).toInt()

        rectDest.apply {
            top = 0
            bottom = this@AvatarBaseViewV3.height
            left = ((this@AvatarBaseViewV3.width - scaledWidth) / 2f).toInt()
            right = left + scaledWidth
        }
    }

    /**
     * Зависит от rectDest
     */
    private fun rectVisibleUpdate() {
        rectVisible.apply {
            top = 0
            bottom = this@AvatarBaseViewV3.height
            left =
                if (rectDest.left <= 0) 0 else rectDest.left
            right =
                if (rectDest.right >= this@AvatarBaseViewV3.width) this@AvatarBaseViewV3.width else rectDest.right
        }
    }

    override fun onBitmapReady(bitmap: Bitmap) {
        sourceImageBitmap = bitmap
        rectBitmapVisible = Rect(0, 0, bitmap.width, bitmap.height)
    }

    /**
     * После события ACTION_UP вызвать этот метод для оповещения внешних слушателей об
     * изменении размера viewPort'a
     */
    var onScaleChangeListener: ((Float, PointF) -> Unit)? = null

    open fun onScaleChanged(scale: Float, pivot: PointF) {}
}