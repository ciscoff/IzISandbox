package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_02

import android.graphics.Rect
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_02.InfiniteModel.Companion.VIEW_PORT_CAPACITY
import kotlin.math.abs

/**
 * Алгоритм.
 * Используются три ключевых компонента:
 * - Модель InfiniteModel (она же OverScrollListener)
 * - ViewHolderDate
 * - данный LayoutManager
 *
 * Основная идея: в качестве позоции в адаптере используется смещение в днях от текущей даты.
 * Сегодня имеет смещение 0 (и позицию 0), завтра: смещение 1 (и позицию 1). вчера: смещение (-1),
 * но позицию 1. Как видно из примера - смещение знаковое, так как пикер листает календарь вперед
 * и назад от сегодня. Однако мы не можем вызывать getViewForPosition с отрицательным аргументом.
 * На помощь приходит OverScrollListener. Через него мы заранее сообщаем в InfiniteModel "в какую
 * сторону" она должна высчитывать очередное значение при следующем запросе от адаптера. Адаптер
 * всегда обращается к модели с положительным индексом, но модель значет какой реальный знак
 * у этого индекса.
 */
class LoopRecyclerManagerV02(private val overScrollListener: OverScrollListener) :
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

    /**
     * Метод, который располагает элементы внутри RecyclerView.
     * Основная задача: центральным должен оказаться элемент с сегодняшней датой. То есть он должен
     * быть не вверху в начале списка, а в центре.
     */
    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State?) {

        /**
         * Сразу отключаем кэш у Recycler'а, чтобы он при каждом getViewForPosition
         * обращался к адаптеру. Адаптер будет дергать модель, которая выдаст правильные данные.
         *
         * NOTE: Ниже в комментах описано, что это не спасает. Нужно разобраться с кешированием.
         */
        recycler.setViewCacheSize(0)

        // Адаптер пустой или стал пустым после обновления модели
        if (itemCount <= 0) {
            detachAndScrapAttachedViews(recycler)
            return
        }

        if (state?.isPreLayout == true) return

        /**
         * NOTE: Метод onLayoutChildren вызывается дважды. Причем в первый раз ещё никаких дочерних
         * View нет, метод detachAndScrapAttachedViews бесполезен и layout проходит правильно:
         * элементы с прошедшими датами располагаются сверху, а с будущими снизу.
         * Однако при повторном onLayoutChildren снова вызывается detachAndScrapAttachedViews,
         * Views уже есть и они кэшируются и потом извлекаются из кэша с помощью getViewForPosition.
         * Проблема в том, что в нашем случае у всех View, кроме центральной дублируется position,
         * плюс они извлекаются из кэша в неправильном порядке, задом наперед. Короче, чтобы этого
         * избежать я после detachAndScrapAttachedViews поставил recycler.clear(), который вычищает
         * кэш и все работает ОК.
         *
         * NOTE++: Решил проблему по другому: создал свой кэш cachedChildren. Туда попадают все
         * Views после getViewForPosition, потом сортируются по возрастанию по значению своего tag
         * и только после сортировки в правильном порядке проходят measure/layout.
         */
        detachAndScrapAttachedViews(recycler)
        //recycler.clear()

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

    /**
     * Порядок действий должен быть такой:
     * 1. Через вызов scrollVerticallyBy система сообщила, что палец двинулся по экрану на dy
     * 2. Мы должны проскроллить (offsetChildrenVertical) именно на это значение, потому что
     *    у нас Infinite Loop
     * 3. Выполнить очистку - ставшие невидимыми View удалить.
     * 4. После скрола нужно проверить "крайние" условия и добавить новые Views сверху/снизу
     *    если требуется.
     * 5. Опциональные плюшки, например визуальные эффекты, завязанные на изменение положения View.
     */
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

    /**
     * Управляем заполнением пустых мест, которые образовались в результате сдвига по скролу,
     * новыми Views. При сильном толчке пальцем дистанция dY может превышать высоту одной или
     * нескольких View, поэтому используются методы fillUp/fillDown, которые делают работу в цикле
     * и устанавливают столько дополнительных Views сколько требуется (в изнечальном варианте
     * добавлялось по одной)
     */
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

    /**
     * Заполнить пространство сверху
     */
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