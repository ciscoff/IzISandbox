package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop

import androidx.recyclerview.widget.RecyclerView

class LoopRecyclerManager : RecyclerView.LayoutManager() {

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

        // Адаптер пустой или стал пустым после обновления модели
        if (itemCount <= 0) {
            detachAndScrapAttachedViews(recycler)
            return
        }

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

    /**
     * Порядок действий должен быть такой:
     * 1. Через вызов scrollVerticallyBy система сообщила, что палец двинулся по экрану на dy
     * 2. Мы должны проскроллить (offsetChildrenVertical) именно на это значение, потому что
     *    у нас Infinite Loop
     * 3. После скрола нужно проверить "крайние" условия и добавить новые Views сверху/снизу
     *    если стребуется.
     * 4. Выполнить очистку - ставшие невидимыми View удалить.
     */
    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State?
    ): Int {

        // 2.
        offsetChildrenVertical(-dy)
        // 3.
        fill(dy, recycler, state)
        // 4.
        recycleInvisibleViews(dy, recycler, state)
        return dy
    }

    private fun fill(dY: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State?) {

        val lastIndex = childCount - 1

        when {
            // Палец идет вверх. Контролируем появление View снизу.
            (dY > 0) -> {
                val lastChild = getChildAt(lastIndex)
                lastChild ?: return

                // Позиция в адаптере
                val lastChildPos = getPosition(lastChild)

                // Если последняя видимая View полностью вошла в экран....
                if (getDecoratedBottom(lastChild) < height) {

                    // ...и является последней в адаптере
                    val scrap = if (lastChildPos == (itemCount - 1)) {
                        recycler.getViewForPosition(0)
                    }
                    // ...и НЕ является последней в адаптере
                    else {
                        recycler.getViewForPosition(lastChildPos + 1)
                    }

                    addView(scrap)
                    measureChildWithMargins(scrap, 0, 0)
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
                }
            }
            // Палец идет вниз. Контролируем появление View сверху.
            (dY < 0) -> {
                val firstChild = getChildAt(0)
                firstChild ?: return

                // Позиция в адаптере
                val firstChildPos = getPosition(firstChild)

                // Если первая видимая View полностью вошла в экран....
                if (firstChild.top >= 0) {
                    val scrap = if (firstChildPos == 0) {
                        recycler.getViewForPosition(itemCount - 1)
                    } else {
                        recycler.getViewForPosition(firstChildPos - 1)
                    }

                    addView(scrap, 0)
                    measureChildWithMargins(scrap, 0, 0)
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
                }
            }
            else -> {
            }

        }
    }

    /**
     * Невидимые Views в утилизацию
     */
    private fun recycleInvisibleViews(
        dY: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State?
    ) {
        (0 until childCount).forEach { i ->
            getChildAt(i)?.let { view ->

                // Проверяем два условия:
                // Палец идет вверх. Ищем Views, которые скрылись за верхней границей (view.bottom < 0)
                // Палец идет вниз. Ищем Views, которые скрылись за нижней границей (view.top > height)
                if ((dY > 0 && view.bottom < 0) || (dY < 0 && view.top > height)) {
                    removeAndRecycleView(view, recycler)
                }
            }
        }
    }
}