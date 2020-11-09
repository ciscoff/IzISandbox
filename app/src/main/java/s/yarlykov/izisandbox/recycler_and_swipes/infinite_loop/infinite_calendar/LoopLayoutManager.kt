package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_calendar

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_calendar.ModelBase.Companion.VIEW_PORT_CAPACITY

/**
 */
class LoopLayoutManager : BaseLayoutManager() {

    /**
     * Метод, который располагает элементы внутри RecyclerView
     */
    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State?) {

        // Адаптер пустой или стал пустым после обновления модели
        if (itemCount <= 0) {
            detachAndScrapAttachedViews(recycler)
            return
        }

        if (state?.isPreLayout == true) return

        detachAndScrapAttachedViews(recycler)
        var summaryHeight = 0

        val viewHeight = height / VIEW_PORT_CAPACITY
        val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(viewHeight, View.MeasureSpec.EXACTLY)

        /**
         * Для корректного выхода из forEach()
         * ref: https://kotlinlang.org/docs/reference/returns.html
         */
        run loop@{
            (0 until itemCount).forEach { i ->
                val child = recycler.getViewForPosition(i)
                addView(child)
                measureChildWithoutInsets(child, widthSpec, heightSpec)
                val (w, h) = getDecoratedMeasuredWidth(child) to getDecoratedMeasuredHeight(child)
                layoutDecorated(child, 0, summaryHeight, w, h + summaryHeight)
                summaryHeight += h

                if (summaryHeight > height) return@loop
            }
        }

        trackRelativeCenter(alphaTuner, scaleTuner)
    }

    override fun fillUp(recycler: RecyclerView.Recycler) {

        val viewHeight = height / VIEW_PORT_CAPACITY
        val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(viewHeight, View.MeasureSpec.EXACTLY)

        var isContinue = true

        do {
            getChildAt(0)?.let { firstChild ->
                // Если первая видимая View полностью вошла в экран....
                if (getDecoratedTop(firstChild) > 0) {
                    // Позиция в адаптере
                    val firstChildPos = getPosition(firstChild)

                    val scrap = if (firstChildPos == 0) {
                        recycler.getViewForPosition(itemCount - 1)
                    } else {
                        recycler.getViewForPosition(firstChildPos - 1)
                    }

                    addView(scrap, 0)
                    measureChildWithoutInsets(scrap, widthSpec, heightSpec)
                    val (w, h) = getDecoratedMeasuredWidth(scrap) to getDecoratedMeasuredHeight(
                        scrap
                    )
                    layoutDecorated(
                        scrap,
                        0,
                        getDecoratedTop(firstChild) - h,
                        w,
                        getDecoratedTop(firstChild)
                    )

                } else {
                    isContinue = false
                }
            } ?: run { isContinue = false }

        } while (isContinue)
    }

    /**
     * Заполнить пространство снизу
     */
    override fun fillDown(recycler: RecyclerView.Recycler) {
        val viewHeight = height / VIEW_PORT_CAPACITY
        val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(viewHeight, View.MeasureSpec.EXACTLY)

        var isContinue = true

        do {
            val lastIndex = childCount - 1

            getChildAt(lastIndex)?.let { lastChild ->

                // Если последняя видимая View полностью вошла в экран....
                if (getDecoratedBottom(lastChild) < height) {

                    val lastChildPos = getPosition(lastChild)

                    // ...и является последней в адаптере
                    val scrap = if (lastChildPos == (itemCount - 1)) {
                        recycler.getViewForPosition(0)
                    }
                    // ...и НЕ является последней в адаптере
                    else {
                        recycler.getViewForPosition(lastChildPos + 1)
                    }

                    addView(scrap)
                    measureChildWithoutInsets(scrap, widthSpec, heightSpec)
                    val (w, h) = getDecoratedMeasuredWidth(scrap) to getDecoratedMeasuredHeight(
                        scrap
                    )
                    layoutDecorated(
                        scrap,
                        0,
                        getDecoratedBottom(lastChild),
                        w,
                        h + getDecoratedBottom(lastChild)
                    )

                } else {
                    isContinue = false
                }

            } ?: run { isContinue = false }

        } while (isContinue)
    }
}