package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class LoopRecyclerManager : RecyclerView.LayoutManager() {

    private var looperEnable = true

    /**
     * Хотим скролиться по вертикали
     */
    override fun canScrollVertically(): Boolean {
        return true
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }

    /**
     * Метод, который располагает элементы внутри RecyclerView
     */
    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State?) {

        if (itemCount <= 0) return
        if (state?.isPreLayout == true) return

        detachAndScrapAttachedViews(recycler)

        var summaryHeight = 0

        (0 until itemCount).forEach { i ->
            val child = recycler.getViewForPosition(i)
            addView(child)
            measureChildWithMargins(child, 0, 0)
            val (w, h) = getDecoratedMeasuredWidth(child) to getDecoratedMeasuredHeight(child)
            layoutDecorated(child, 0, summaryHeight, w, h + summaryHeight)
            summaryHeight += h

            if (summaryHeight > height) return@forEach
        }
    }

    private fun fill(dy: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State?): Int {

        val lastIndex = childCount - 1
        var offsetY = dy

        // Палец идет вверх. Контролируем появление View снизу.
        if (dy > 0) {

            val lastView = getChildAt(lastIndex)
            lastView ?: return 0

            // Позиция в адаптере
            val lastPos = getPosition(lastView)

            // Если последняя видимая View полностью вошла в экран ....
            if (lastView.bottom < height) {
                var scrap: View? = null

                // ... и является последней в адаптере
                if (lastPos == lastIndex) {
                    if (looperEnable) {
                        scrap = recycler.getViewForPosition(0)
                    } else {
                        offsetY = 0
                    }

                }
                // ... и НЕ является последней в адаптере
                else {
                    scrap = recycler.getViewForPosition(lastPos + 1)
                }

                scrap ?: return offsetY

                addView(scrap)
                measureChildWithMargins(scrap, 0, 0)
                val (w, h) = getDecoratedMeasuredWidth(scrap) to getDecoratedMeasuredHeight(scrap)
                layoutDecorated(scrap, 0, lastView.bottom, w, h + lastView.bottom)

            }




        }


        return offsetY
    }

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        return super.scrollVerticallyBy(dy, recycler, state)
    }
}