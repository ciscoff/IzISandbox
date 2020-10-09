package s.yarlykov.izisandbox.telegram.v2

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import s.yarlykov.izisandbox.R
import kotlin.math.abs

class ActionBarLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val minHeight: Float
    private val maxHeight: Float

    init {
        maxHeight = context.resources.getDimension(R.dimen.avatar_max_height)
        minHeight = context.resources.getDimension(R.dimen.avatar_min_height)
    }

    /**
     * Палец тянут вверх
     *
     * NOTE: скроллинг контента становится возможным только если пальцем дотолкали AppBar
     * до минимальной высоты. Тогда скроллинг открывается и можно продолжать прокрутку
     * контента вверх. То есть в этот момент мы как бы состкакиваем с зацепа.
     */
    private fun siblingScrollingUp(dy: Int) {
        var isOwnHeightChanged: Boolean

        layoutParams.apply {
            val h = measuredHeight - dy

            if (h <= minHeight) {
                isOwnHeightChanged = measuredHeight != minHeight.toInt()
                height = minHeight.toInt()
                scrollingAllowed = true
            } else {
                height = measuredHeight - dy
                isOwnHeightChanged = true
                scrollingAllowed = false
            }
        }

        if (isOwnHeightChanged) requestLayout()
    }

    /**
     * Палец тянут вниз
     */
    private fun siblingScrollingDown(dy: Int) {

        var isOwnHeightChanged: Boolean
        scrollingAllowed = false

        layoutParams.apply {

            val h = measuredHeight + dy

            if (h >= maxHeight) {
                isOwnHeightChanged = measuredHeight != maxHeight.toInt()
                height = maxHeight.toInt()
            } else {
                height = measuredHeight + dy
                isOwnHeightChanged = true
            }
        }
        if (isOwnHeightChanged) requestLayout()
    }

    var scrollingAllowed: Boolean = false

    fun onOffsetChanged(offset: Int) {
        if (offset < 0) {
            siblingScrollingUp(abs(offset))
        } else if (offset > 0) {
            siblingScrollingDown(abs(offset))
        }
    }
}