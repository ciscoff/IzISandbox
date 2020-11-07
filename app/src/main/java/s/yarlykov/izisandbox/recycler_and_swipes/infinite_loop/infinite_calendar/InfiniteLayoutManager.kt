package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_calendar

import android.graphics.Rect
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_calendar.ModelDate.Companion.VIEW_PORT_CAPACITY
import kotlin.math.abs

class InfiniteLayoutManager(private val overScrollListener: OverScrollListener) :
    RecyclerView.LayoutManager() {

    private val cachedChildren = mutableListOf<View>()

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

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State?) {

        recycler.setViewCacheSize(0)

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

    override fun scrollVerticallyBy(
        dY: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State?
    ): Int {

        // 2.
        offsetChildrenVertical(-dY)
        // 3.
        recycleInvisibleViews(dY, recycler, state)
        // 4.
        fill(dY, recycler, state)

        // 5. Опционально. Меняем прозрачность элемента и размер текста внутри.
        trackRelativeCenter(alphaTuner, scaleTuner)
        return dY
    }

    private fun fill(dY: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State?) {
        when {
            // Палец идет вверх. Контролируем появление View снизу.
            (dY > 0) -> fillDown(recycler)
            // Палец идет вниз. Контролируем появление View сверху.
            (dY < 0) -> fillUp(recycler)
            else -> {
            }
        }
    }

    private fun fillUp(recycler: RecyclerView.Recycler) {
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
    private fun fillDown(recycler: RecyclerView.Recycler) {
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
                if ((dY > 0 && getDecoratedBottom(view) <= 0) ||
                    (dY < 0 && getDecoratedTop(view) >= height)
                ) {
                    removeAndRecycleView(view, recycler)
                }
            }
        }
    }

    private fun trackRelativeCenter(vararg handlers: (View, Float) -> Unit) {

        val pivot = height / 2f
        if (pivot == 0f) return

        (0 until childCount).forEach { i ->
            getChildAt(i)?.let { child ->

                var viewCenter =
                    (getDecoratedBottom(child) - getDecoratedTop(child)) / 2f +
                            getDecoratedTop(child)

                viewCenter = when {
                    (viewCenter < 0) -> 0f
                    (viewCenter > height) -> height.toFloat()
                    else -> viewCenter
                }

                val relation = when {
                    viewCenter < pivot -> {
                        viewCenter / pivot
                    }
                    viewCenter > pivot -> {
                        2f - viewCenter / pivot
                    }
                    else -> 1f
                }
                handlers.forEach { it(child, relation) }
            }
        }
    }

    /**
     * Установить alpha
     */
    private val alphaTuner: (View, Float) -> Unit = { view, alpha ->
        view.alpha = alpha
    }

    /**
     * Установить масштаб. Здесь для нужной View устанавливается масштаб равный (1 + maxDelta).
     * То есть размер View постепенно меняется между значениями 1 и (1 + maxDelta), а maxDelta
     * меняется в диапазоне от 0 до 0.8 благодаря тому, что аргумент scale меняется от 0 до 1.
     */
    private val scaleTuner: (View, Float) -> Unit = { view, scale ->
        val maxDelta = 0.6f

        view.findViewById<TextView>(R.id.textTitle)?.let { child ->

            val factor = scale * maxDelta + 1f

            child.scaleX = factor
            child.scaleY = factor
        }
    }

    /**
     * Получить размер view с учетом всех insets, а именно отступов, которые насчитал декоратор,
     * а также маргинов нашей view
     */
    private fun measureChildWithoutInsets(child: View, widthSpec: Int, heightSpec: Int) {

        val decorRect = Rect()

        // У декоратора запрашиваем инсеты для view и получаем их в Rect
        calculateItemDecorationsForChild(child, decorRect)

        val lp = child.layoutParams as RecyclerView.LayoutParams

        val widthSpecUpdated = updateMeasureSpecs(
            widthSpec,
            lp.leftMargin + decorRect.left,
            lp.rightMargin + decorRect.right
        )

        val heightSpecUpdated = updateMeasureSpecs(
            heightSpec,
            lp.topMargin + decorRect.top,
            lp.bottomMargin + decorRect.bottom
        )
        child.measure(widthSpecUpdated, heightSpecUpdated)
    }

    /**
     * Корректируем отдельную размерность (ширина/высота) view с учетом имеющихся insets.
     */
    private fun updateMeasureSpecs(spec: Int, startInset: Int, endInset: Int): Int {
        if (startInset == 0 && endInset == 0) {
            return spec
        }

        val mode = View.MeasureSpec.getMode(spec)

        if (mode == View.MeasureSpec.AT_MOST || mode == View.MeasureSpec.EXACTLY) {
            return View.MeasureSpec.makeMeasureSpec(
                View.MeasureSpec.getSize(spec) - startInset - endInset, mode
            )
        }
        return spec
    }
}