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

    var mDecoratedChildWidth : Int = 0
    var mDecoratedChildHeight : Int = 0

    /**
     * Сделать layout элементов одной строки
     */
    private fun fillRow(recycler: RecyclerView.Recycler): Int {

        var nextItem = 0
        summaryWidth = 0

        val widthSpec = View.MeasureSpec.makeMeasureSpec(spanSize, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)

        if(childCount == 0 ){
            val scrap = recycler.getViewForPosition(0)
            addView(scrap)
            measureChildWithMargins(scrap, 0, 0)

            mDecoratedChildWidth = getDecoratedMeasuredWidth(scrap)
            mDecoratedChildHeight = getDecoratedMeasuredHeight(scrap)

            detachAndScrapView(scrap, recycler)
        }


        var viewLeft = paddingLeft
        var viewTop = paddingTop

        while (nextItem != itemCount && viewLeft < width) {

            val child = recycler.getViewForPosition(nextItem)
            addView(child)

            measureChildWithMargins(child, 0, 0)

//            measureChildWithoutInsets(child, widthSpec, heightSpec)

            val decoratedMeasuredWidth = getDecoratedMeasuredWidth(child)
            val decoratedMeasuredHeight = getDecoratedMeasuredHeight(child)
            layoutDecorated(child, viewLeft, viewTop, viewLeft + decoratedMeasuredWidth, viewTop + decoratedMeasuredHeight)
            viewLeft = getDecoratedRight(child)
            nextItem++
        }

//        val child = recycler.getViewForPosition(i)
//
//        addView(child)
//        measureChildWithoutInsets(child, widthSpec, heightSpec)
//        child.layout(leftFrom, topFrom, leftFrom + spanSize, topFrom + spanSize)
//        leftFrom += (spanSize + spacer)
//
//
//        summaryWidth += spanSize
//
//        return positionTo
        return 0
    }


    /**
     * Измерить view с учетом имеющихся insets, а именно:
     * - отступов, которые насчитал декоратор
     * - margins нашей view
     */
    private fun measureChildWithoutInsets(child: View, widthSpec: Int, heightSpec: Int) {

        val decorRect = Rect()

        // Получить декор
        calculateItemDecorationsForChild(child, decorRect)

        resetMargins(child)

        val widthSpecUpdated = updateMeasureSpecs(widthSpec, spanSize)
        val heightSpecUpdated = updateMeasureSpecs(heightSpec, spanSize)
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


    override fun canScrollHorizontally(): Boolean = true
    override fun canScrollVertically(): Boolean = true

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }
}