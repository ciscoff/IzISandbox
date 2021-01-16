package s.yarlykov.izisandbox.recycler_and_swipes.grid

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class CellView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /**
     * Paint (color Yellow)
     */
    private val paintFill: Paint = Paint().apply {
        color = Color.argb(0xff, 0xff, 0xff, 0x0)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val spec = MeasureSpec.makeMeasureSpec(w / 4, MeasureSpec.EXACTLY)
        super.onMeasure(spec, spec)
    }

    override fun onDraw(canvas: Canvas) {
        val r = min(width, height) / 4f

        canvas.drawColor(Color.DKGRAY)
        canvas.drawCircle(width / 2f, height / 2f, r, paintFill)
    }
}