package s.yarlykov.izisandbox.matrix.avatar_maker_dev

import android.content.Context
import android.graphics.Bitmap
import android.graphics.LightingColorFilter
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

abstract class AvatarBaseView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), MediaDataConsumer {

    /**
     * Цветовые фильтры поярче/потемнее.
     */
    protected val colorFilterLighten = LightingColorFilter(0xFFFFFFFF.toInt(), 0x00222222)
    protected val colorFilterDarken = LightingColorFilter(0xFF7F7F7F.toInt(), 0x00000000)

    /**
     * @rectDest прямоугольник (в координатах канвы) куда будем рисовать исходную Bitmap.
     * Он может выходить краями за пределы экрана.
     */
    protected val rectDest = Rect()

    /**
     * @rectVisible прямоугольник (в координатах канвы) определяющий видимую часть
     * картинки. Все что не влезло в экран не учитывается.
     */
    protected val rectVisible = Rect()

    /**
     * Исходная Bitmap и её размеры
     */
    protected var sourceImageBitmap: Bitmap? = null
    protected var rectSourceImage = Rect()

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

        val ratio = height.toFloat() / rectSourceImage.height()
        val scaledWidth = (rectSourceImage.width() * ratio).toInt()

        rectDest.apply {
            top = 0
            bottom = this@AvatarBaseView.height
            left = ((this@AvatarBaseView.width - scaledWidth) / 2f).toInt()
            right = left + scaledWidth
        }
    }

    /**
     * Зависит от rectDest
     */
    private fun rectVisibleUpdate() {
        rectVisible.apply {
            top = 0
            bottom = this@AvatarBaseView.height
            left =
                if (rectDest.left <= 0) 0 else rectDest.left
            right =
                if (rectDest.right >= this@AvatarBaseView.width) this@AvatarBaseView.width else rectDest.right
        }
    }


    override fun onBitmapReady(bitmap: Bitmap) {
        sourceImageBitmap = bitmap
        rectSourceImage = Rect(0, 0, bitmap.width, bitmap.height)
    }
}