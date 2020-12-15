package s.yarlykov.izisandbox.matrix.avatar_maker_v1

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.time_line.domain.SeverityMode

class AvatarBackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AvatarBaseView(context, attrs, defStyleAttr) {

    /**
     * Paint для заливки фона исходной Bitmap'ой с применением цветового фильтра.
     */
    private var paintBackground : Paint? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        sourceImageBitmap?.let {
            canvas.drawBitmap(it, rectSourceImage, rectDest, paintBackground)
        }
    }

    fun setDarkPaint() {
        paintBackground = Paint(Color.GRAY).apply { colorFilter = colorFilterDarken }
    }
}