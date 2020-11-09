package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_calendar

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_calendar.ModelBase.Companion.VIEW_PORT_CAPACITY
import kotlin.math.abs

class InfiniteLayoutManager(private val overScrollListener: OverScrollListener) :
    BaseLayoutManager() {

    /**
     * Свой кэш дочерних Views для их предварительной сортировки перед layout'ом.
     * Сортировка по возрастанию по сзначению View.tag.
     */
    private val cachedChildren = mutableListOf<View>()

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State?) {

        recycler.setViewCacheSize(0)

        // Адаптер пустой или стал пустым после обновления модели
        if (itemCount <= 0) {
            detachAndScrapAttachedViews(recycler)
            return
        }

        if (state?.isPreLayout == true) return

        detachAndScrapAttachedViews(recycler)

        // Индекс позиции, располагаемой по центру вертикально (VIEW_PORT_CAPACITY нечетная)
        // Она соответствует элементу с сегодняшней датой.
        val centerPosition = VIEW_PORT_CAPACITY / 2

        cachedChildren.clear()

        /**
         * Для корректного выхода из forEach()
         * ref: https://kotlinlang.org/docs/reference/returns.html
         *
         * Заполняем собственный кэш
         */
        (0 until VIEW_PORT_CAPACITY)
            .map { position -> position - centerPosition }
            .forEach { i ->
                overScrollListener.setOffsetDirection(i)
                val child = recycler.getViewForPosition(abs(i))
                cachedChildren.add(child)
            }

        layoutCachedChildren()

        trackRelativeCenter(alphaTuner, scaleTuner)
    }

    /**
     * Сортируем элементы в кэше и выполняем их layout
     */
    private fun layoutCachedChildren() {
        var summaryHeight = 0
        val viewHeight = height / VIEW_PORT_CAPACITY
        val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(viewHeight, View.MeasureSpec.EXACTLY)

        // Сортировка по возрастанию значений в тегах.
        cachedChildren.sortBy {
            it.tag as Int
        }

        run loop@{
            cachedChildren.forEach { child ->
                addView(child)
                measureChildWithoutInsets(child, widthSpec, heightSpec)
                val (w, h) = getDecoratedMeasuredWidth(child) to
                        getDecoratedMeasuredHeight(child)
                layoutDecorated(child, 0, summaryHeight, w, h + summaryHeight)
                summaryHeight += h

                if (summaryHeight >= height) return@loop
            }
        }
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

                    val firstChildOffset = firstChild.tag as Int
                    val nextChildOffset = firstChildOffset - 1

                    overScrollListener.setOffsetDirection(nextChildOffset)

                    val scrap = recycler.getViewForPosition(abs(nextChildOffset))

                    addView(scrap, 0)
                    measureChildWithoutInsets(scrap, widthSpec, heightSpec)
                    val (w, h) =
                        getDecoratedMeasuredWidth(scrap) to getDecoratedMeasuredHeight(scrap)

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

                    val lastChildOffset = lastChild.tag as Int
                    val nextChildOffset = lastChildOffset + 1

                    overScrollListener.setOffsetDirection(nextChildOffset)

                    val scrap = recycler.getViewForPosition(abs(nextChildOffset))
                    addView(scrap)
                    measureChildWithoutInsets(scrap, widthSpec, heightSpec)

                    val (w, h) =
                        getDecoratedMeasuredWidth(scrap) to getDecoratedMeasuredHeight(scrap)

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