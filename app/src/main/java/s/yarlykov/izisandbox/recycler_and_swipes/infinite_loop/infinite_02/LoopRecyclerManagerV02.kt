package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_02

import android.graphics.Rect
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.Utils.logIt
import s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_02.InfiniteDatePickerActivity.Companion.MODEL_SIZE

class LoopRecyclerManagerV02(private val overScrollListener: OverScrollListener) :
    RecyclerView.LayoutManager() {

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

        recycler.setViewCacheSize(0)

        // Адаптер пустой или стал пустым после обновления модели
        if (itemCount <= 0) {
            detachAndScrapAttachedViews(recycler)
            return
        }

        if (state?.isPreLayout == true) return

        detachAndScrapAttachedViews(recycler)
        var summaryHeight = 0
        val viewHeight = height / MODEL_SIZE

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

                if (summaryHeight >= height) return@loop
            }
        }

//        trackRelativeCenter(alphaTuner, scaleTuner)
    }

    /**
     * Порядок действий должен быть такой:
     * 1. Через вызов scrollVerticallyBy система сообщила, что палец двинулся по экрану на dy
     * 2. Мы должны проскроллить (offsetChildrenVertical) именно на это значение, потому что
     *    у нас Infinite Loop
     * 3. После скрола нужно проверить "крайние" условия и добавить новые Views сверху/снизу
     *    если стребуется.
     * 4. Выполнить очистку - ставшие невидимыми View удалить.
     * 5. Опциональные плюшки, например визуальные эффекты, завязанные на изменение положения View.
     */
    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State?
    ): Int {

        // 2.
        offsetChildrenVertical(-dy)
        // 4.
        recycleInvisibleViews(dy, recycler, state)
        // 3.
        fill(dy, recycler, state)

        // 5. Опционально. Меняем прозрачность элемента и размер текста внутри.
//        trackRelativeCenter(alphaTuner, scaleTuner)
        return dy
    }

    private fun fill(dY: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State?) {

        val lastIndex = childCount - 1

        val viewHeight = height / MODEL_SIZE

        val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(viewHeight, View.MeasureSpec.EXACTLY)

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
                        overScrollListener.onBottomOverScroll(lastChild.tag as Long)
                        recycler.getViewForPosition(itemCount - 1)
                    }
                    // ...и НЕ является последней в адаптере
                    else {
                        recycler.getViewForPosition(lastChildPos + 1)
                    }

                    addView(scrap)
                    measureChildWithoutInsets(scrap, widthSpec, heightSpec)
//                    measureChildWithMargins(scrap, 0, 0)
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
                if (getDecoratedTop(firstChild) >= 0) {
                    val scrap = if (firstChildPos == 0) {
                        overScrollListener.onTopOverScroll(firstChild.tag as Long)
                        recycler.getViewForPosition(0)
                    } else {
                        recycler.getViewForPosition(firstChildPos - 1)
                    }

                    addView(scrap, 0)
                    measureChildWithoutInsets(scrap, widthSpec, heightSpec)
//                    measureChildWithMargins(scrap, 0, 0)
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
                if ((dY > 0 && getDecoratedBottom(view) <= 0) ||
                    (dY < 0 && getDecoratedTop(view) >= height)
                ) {
                    logIt("Removing view for position=${getPosition(view)}", "PLPLPL")
                    removeAndRecycleView(view, recycler)

                    // Это совсем на крайний случай
//                    recycler.clear()
                }
            }
        }
    }

    /**
     * Для каждого дочернего View функция расчитывает отношение его вертикального центра
     * к вертикальному центру родительского RecyclerView. Фактически это "степень" приближения к
     * центру RecyclerView.
     * 1 - говорит, что центры совпадают.
     * 0 - дочерняя View центрирована по верхней или нижней границе родительского контейнера.
     * 0.xxx - промежуточное значение.
     *
     * На основании полученного результата к View можно применить какой-нибудь визуальный эффект,
     * например менять прозрачность или масштабирование.
     *
     * @handlers - массив функций, применяющих визуальные эффекты.
     */
    private fun trackRelativeCenter(vararg handlers: (View, Float) -> Unit) {

        val pivot = height / 2f
        if (pivot == 0f) return

        (0 until childCount - 1).forEach { i ->
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
        val maxDelta = 0.8f

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