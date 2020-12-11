package s.yarlykov.izisandbox.matrix.avatar_maker

import android.content.Context
import android.graphics.*
import android.util.AttributeSet

class AvatarBackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AvatarBaseView(context, attrs, defStyleAttr) {

    /**
     * Paint для заливки фона исходной Bitmap'ой с применением цветового фильтра.
     */
    private val paintBackground = Paint(Color.GRAY).apply { colorFilter = colorFilterDarken }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        sourceImageBitmap?.let {
            canvas.drawBitmap(it, rectSourceImage, rectDest, paintBackground)
        }
    }
}