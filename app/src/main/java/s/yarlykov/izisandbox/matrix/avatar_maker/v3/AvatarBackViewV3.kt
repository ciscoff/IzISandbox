package s.yarlykov.izisandbox.matrix.avatar_maker.v3

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import s.yarlykov.izisandbox.matrix.avatar_maker.AvatarBaseView

class AvatarBackViewV3 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AvatarBaseViewV3(context, attrs, defStyleAttr) {

    /**
     * Paint для заливки фона исходной Bitmap'ой с применением цветового фильтра.
     */
    private var paintBackground: Paint? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        invalidate()
    }

    /**
     * @rectSourceImage - задает исходный прямоугольник в координатах битмапы
     * @rectDest - целевой прямоугольник в координатах канвы.
     * То есть берем некую часть из битмапы и переносим в указанную область канвы.
     */
    override fun onDraw(canvas: Canvas) {
        sourceImageBitmap?.let {
            canvas.drawBitmap(it, rectSourceImage, rectDest, paintBackground)
        }
    }

    override fun onScaleChanged(scale: Float) {
        if (scale > 1f) return

        sourceImageBitmap?.let { bitmap ->
            rectSourceImage.right = (bitmap.width * scale).toInt()
            rectSourceImage.bottom = (bitmap.height * scale).toInt()
            invalidate()
        }
    }
}