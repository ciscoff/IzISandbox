package s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d

import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R

class TimeLineLayoutManager(val context: Context) : RecyclerView.LayoutManager() {

    /**
     * Переменная для layout'а наборов строк.
     */
    private var summaryWidth = 0

    /**
     * Ширина отдельного элемента
     */
    private val spanSize: Int by lazy {
        context.resources.getDimensionPixelSize(R.dimen.column_width)
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

        fill(recycler)
    }

    private fun fill(recycler: RecyclerView.Recycler) {
        fillRow(recycler)
    }

    /**
     * Сделать layout элементов одной строки
     */
    private fun fillRow(recycler: RecyclerView.Recycler) {

        var nextItem = 0
        summaryWidth = 0

        val widthSpec = View.MeasureSpec.makeMeasureSpec(spanSize, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)

        var viewLeft = paddingLeft
        var viewTop = paddingTop

        while (nextItem != itemCount && viewLeft < width) {

            val child = recycler.getViewForPosition(nextItem)
            addView(child)

            measureChildWithoutInsets(child, widthSpec, heightSpec)

            val decoratedMeasuredWidth = getDecoratedMeasuredWidth(child)
            val decoratedMeasuredHeight = getDecoratedMeasuredHeight(child)

            layoutDecorated(
                child,
                viewLeft,
                viewTop,
                viewLeft + decoratedMeasuredWidth,
                viewTop + decoratedMeasuredHeight
            )
            viewLeft = getDecoratedRight(child)
            nextItem++
        }
    }


    /**
     * Установить "чистый" размер view - без inset'ов, а именно:
     * - отступов, которые насчитал декоратор
     * - margins нашей view
     */
    private fun measureChildWithoutInsets(child: View, widthSpec: Int, heightSpec: Int) {

        // Посчитать декор (вызвать обязательно, иначе offset-декоратор не отработает)
        val decorRect = Rect()
        calculateItemDecorationsForChild(child, decorRect)

        resetMargins(child)

        val widthSpecUpdated = updateMeasureSpecs(widthSpec, spanSize)
        val heightSpecUpdated = updateMeasureSpecs(heightSpec, height - paddingTop)
        child.measure(widthSpecUpdated, heightSpecUpdated)
    }

    /**
     * Обнуляем все маргины. Все отступы будут согласно декору.
     */
    private fun resetMargins(child: View) {
        (child.layoutParams as ViewGroup.MarginLayoutParams).apply {
            leftMargin = 0
            rightMargin = 0
            bottomMargin = 0
            topMargin = 0
        }
    }

    /**
     * Установить размер в MeasureSpec
     */
    private fun updateMeasureSpecs(spec: Int, size: Int): Int {

        val mode = View.MeasureSpec.getMode(spec)

        if (mode == View.MeasureSpec.AT_MOST || mode == View.MeasureSpec.EXACTLY) {
            return View.MeasureSpec.makeMeasureSpec(size, mode)
        }
        return spec
    }

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        return super.scrollHorizontallyBy(dx, recycler, state)
    }

    override fun canScrollHorizontally(): Boolean = true
    override fun canScrollVertically(): Boolean = true

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }
}