package s.yarlykov.izisandbox.matrix.avatar_maker

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.screenHeight
import s.yarlykov.izisandbox.extensions.screenWidth

class AvatarPreView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var backgroundBitmap: Bitmap? = null
    private var rectSource = Rect()
    private val rectDest: Rect
        get() = scaleWithRatio()

    private val colorFilterLighten = LightingColorFilter(0xFFFFFFFF.toInt(), 0x00222222)
    private val colorFilterDarken = LightingColorFilter(0xFF7F7F7F.toInt(), 0x00000000)

    private val paint = Paint(Color.GRAY).apply { colorFilter = colorFilterDarken }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        backgroundBitmap =
            BitmapFactory.decodeResource(context.resources, R.drawable.nature)?.also {
                rectSource = Rect(0, 0, it.width, it.height)
            }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        backgroundBitmap?.let {
            canvas.drawBitmap(it, rectSource, rectDest, paint)
        }
    }

    /**
     * Растянуть по высоте с ratio.
     *
     * При любой ориентации "натягиваем" битмапу по высоте. При этом, часть битмапы
     * может оказаться за пределами боковых границ view. Но это фигня. Главное, что
     * нет искажений от растяжки/сжатия.
     */
    private fun scaleWithRatio(): Rect {

        val ratio = height.toFloat() / rectSource.height()
        val scaledWidth = (rectSource.width() * ratio).toInt()

        return Rect().apply {
            top = 0
            bottom = this@AvatarPreView.height
            left = (this@AvatarPreView.width - scaledWidth) / 2
            right = left + scaledWidth
        }
    }

    /**
     * Растянуть по всему экрану
     */
    private fun scaleNoRatio(): Rect {
        return Rect(0, 0, context.screenWidth, context.screenHeight)
    }
}