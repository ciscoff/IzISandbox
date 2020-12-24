package s.yarlykov.izisandbox.recycler_and_swipes.grid

import android.content.Context
import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.utils.logIt

class CellLayoutManager(val context: Context) : RecyclerView.LayoutManager() {

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }

    /**
     * Хотим скролиться по вертикали
     */
    override fun canScrollVertically(): Boolean {
        return true
    }


    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State?) {
        super.onLayoutChildren(recycler, state)

        // Адаптер пустой или стал пустым после обновления модели
        if (itemCount <= 0) {
            detachAndScrapAttachedViews(recycler)
            return
        }

        if (state?.isPreLayout == true) return

        detachAndScrapAttachedViews(recycler)

        fillDown(recycler)
    }

    private fun fillDown(recycler: RecyclerView.Recycler) {

        var summaryHeight = 0

        run loop@{
            (0 until itemCount).forEach { i ->
                val child = recycler.getViewForPosition(i)

                addView(child)
                measureChildWithMargins(child, 0, 0)
                val (w, h) = getDecoratedMeasuredWidth(child) to getDecoratedMeasuredHeight(child)




                layoutDecoratedWithMargins(child, 0, summaryHeight, w, h + summaryHeight)
                summaryHeight += h

                /**
                 * Одни и теже значения в результатах
                 */
                val l = getDecoratedLeft(child)
                val r = getDecoratedRight(child)
                val b = getDecoratedBottom(child)
                val t = getDecoratedTop(child)

                val rect = Rect()
                getDecoratedBoundsWithMargins(child, rect)

                val leftDecW = getLeftDecorationWidth(child)
                val rightDecW = getRightDecorationWidth(child)

                logIt("1. decorated: l,t,r,b=$l,$t,$r,$b. bounds=$rect, leftDecW=$leftDecW, rightDecW=$rightDecW", true, "PLPL")


                if (summaryHeight > height) return@loop
            }
        }

    }
}