package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_calendar

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/**
 * Основной функционал LayoutManager'а. Производные классы InfiniteLayoutManager и
 * LoopLayoutManager реализуют layout дочерних элементов с учетом своей специфики:
 * InfiniteLayoutManager генерит данные бесконечно, LoopLayoutManager гоняет модель по кругу.
 */
abstract class BaseLayoutManager : RecyclerView.LayoutManager() {

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

    /**
     * Порядок действий должен быть такой:
     * 1. Через вызов scrollVerticallyBy система сообщила, что палец двинулся по экрану на dy
     * 2. Мы должны проскроллить (offsetChildrenVertical) именно на это значение, потому что
     *    у нас Infinite Loop
     * 4. Выполнить очистку - ставшие невидимыми View удалить.
     * 4. Проверить "крайние" условия и добавить новые Views сверху/снизу если стребуется.
     * 5. Опциональные плюшки, например визуальные эффекты, завязанные на изменение положения View
     *    относительно вертикального центра RecyclerView.
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
     * После окончания скрола добавляем новые View если это требуется.
     * Если был скролл пальцем вверх, то добавляем снизу. Если пальцем вниз, то сверху.
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

    abstract fun fillDown(recycler: RecyclerView.Recycler)
    abstract fun fillUp(recycler: RecyclerView.Recycler)

    /**
     * Получить "чистый" размер view внутри выделенного ей пространства. Для этого нужно вычесть из
     * размеров этого пространства размеры всех insets: отступы, которые насчитал декоратор, и
     * маргины нашей view.
     */
    protected fun measureChildWithoutInsets(child: View, widthSpec: Int, heightSpec: Int) {

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
                if ((dY > 0 && getDecoratedBottom(view) < 0) ||
                    (dY < 0 && getDecoratedTop(view) > height)
                ) {
                    removeAndRecycleView(view, recycler)
                }
            }
        }
    }

    /**
     * Ближайшая к центру дочерняя View
     */
    val closestToCenter: View?
        get() {
            var closest = -1
            var minDist = Int.MAX_VALUE

            (0 until childCount).forEach { i ->
                getChildAt(i)?.let { child ->
                    val center = childCenterY(child)
                    val dist = abs(height / 2 - center).toInt()
                    if (dist < minDist) {
                        minDist = dist
                        closest = i
                    }
                }
            }

            return if (closest != -1) getChildAt(closest) else null
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
    protected fun trackRelativeCenter(vararg handlers: (View, Float) -> Unit) {

        val pivot = height / 2f
        if (pivot == 0f) return

        (0 until childCount).forEach { i ->
            getChildAt(i)?.let { child ->

                var viewCenter = childCenterY(child)

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
     * Y-координата вертикального центра дочернего элемента
     */
    private fun childCenterY(child: View) =
        (getDecoratedBottom(child) - getDecoratedTop(child)) / 2f +
                getDecoratedTop(child)

    /**
     * Установить alpha (ratio 0..1)
     */
    protected val alphaTuner: (View, Float) -> Unit = { view, ratio ->
        view.alpha = ratio
    }

    /**
     * Максимальное приращение к 1, то есть устанавливаем максимальный порог для scale
     * равным (1 + delta), где 0 <= delta <= scaleDelta.
     */
    private val scaleDelta = 0.3f

    /**
     * Установить масштаб. Здесь для нужной View устанавливается масштаб равный (1 + scaleDelta).
     * То есть размер View постепенно меняется между значениями 1 и (1 + scaleDelta), а scaleDelta
     * меняется в диапазоне от 0 до 0.3 благодаря тому, что аргумент ratio меняется от 0 до 1.
     */
    protected val scaleTuner: (View, Float) -> Unit = { view, ratio ->

        val factor = ratio * scaleDelta + 1f
        view.scaleX = factor
        view.scaleY = factor
    }
}