package s.yarlykov.izisandbox.recycler_and_swipes.debug_events

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.content.ContextCompat
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.utils.logIt

class ItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), ScaleGestureDetector.OnScaleGestureListener {

    private val colorLeft = ContextCompat.getColor(context, R.color.colorDecor6)
    private val colorRight = ContextCompat.getColor(context, R.color.colorDecor11)

    private val tab = "      "

    private val paintRect = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val paintText = Paint().apply {
        color = ContextCompat.getColor(context, android.R.color.white)
        strokeWidth = context.resources.getDimension(R.dimen.title_stroke_width)
        textSize = context.resources.getDimension(R.dimen.title_text_size)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val textLeft = context.resources.getString(R.string.drag_event)
    private val textRight = context.resources.getString(R.string.scroll_event)

    private val rectTextLeft = Rect()
    private val rectTextRight = Rect()
    private val rectBackground = Rect()

    /**
     * Детектор zoom'а
     */
    private val scaleDetector = ScaleGestureDetector(context, this)

    /**
     * У канвы координаты относительно view, а не относительно родительского RecyclerView.
     */
    override fun onDraw(canvas: Canvas) {

        paintText.getTextBounds(textLeft, 0, textLeft.length, rectTextLeft)
        paintText.getTextBounds(textRight, 0, textRight.length, rectTextRight)

        rectBackground.set(0, 0, width / 2, height)
        paintRect.color = colorLeft
        canvas.drawRect(rectBackground, paintRect)

        canvas.drawText(
            textLeft,
            rectBackground.left + (rectBackground.width() - rectTextLeft.width()) / 2f,
            rectBackground.top + (height - rectTextLeft.height()) / 2f + rectTextLeft.height(),
            paintText
        )

        rectBackground.set(width / 2, 0, width, height)
        paintRect.color = colorRight
        canvas.drawRect(rectBackground, paintRect)

        canvas.drawText(
            textRight,
            rectBackground.left + (rectBackground.width() - rectTextRight.width()) / 2f,
            rectBackground.top + (height - rectTextRight.height()) / 2f + rectTextRight.height(),
            paintText
        )
    }

    /**
     * Тач в левой половине
     */
    private fun tapInDrag(event: MotionEvent): Boolean {
        val rect = RectF(0f, 0f, width / 2f, height.toFloat())
        return rect.contains(event.x, event.y)
    }

    private var disallowIntercept = false

    /**
     * event.x / event.y в координатах View
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {

        scaleDetector.onTouchEvent(event)

        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {

                if (tapInDrag(event)) {
                    disallowIntercept = true
                    parent.requestDisallowInterceptTouchEvent(disallowIntercept)
                }

                val result = true
                logIt("${tab}ItemView: ACTION_DOWN return $result")
                result
            }

            // Касание вторым пальцем. Теперь оба пальца на экране.
            MotionEvent.ACTION_POINTER_DOWN -> {
                val result = true
                logIt("${tab}ItemView: ACTION_POINTER_DOWN return $result")
                result
            }

            MotionEvent.ACTION_MOVE -> {
                val result = true
                logIt("${tab}ItemView: ACTION_MOVE return $result")
                result
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val result = true
                logIt("${tab}ItemView: ACTION_POINTER_UP return $result")
                result
            }

            MotionEvent.ACTION_UP -> {
                if (disallowIntercept) {
                    disallowIntercept = false
                }

                val result = true
                logIt("${tab}ItemView: ACTION_UP return $result")
                result
            }
            MotionEvent.ACTION_CANCEL -> {
                val result = true
                logIt("${tab}ItemView: ACTION_CANCEL return $result")
                result
            }
            else -> {
                val result = false
                logIt("${tab}ItemView: UNKNOWN return $result")
                result
            }
        }
    }

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        val result = true
        logIt("${tab}ItemView:onScale return $result")
        return result
    }

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        val result = true
        logIt("${tab}ItemView:onScaleBegin return $result")
        return result
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        logIt("${tab}ItemView:onScaleEnd")
    }
}