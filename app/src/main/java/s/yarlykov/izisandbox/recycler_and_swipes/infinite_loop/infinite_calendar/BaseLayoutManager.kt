package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_calendar

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

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
     * Максимальное приращение к 1, то есть устанавливаем максимальный порог для scale
     * равным (1 + delta), где 0 <= delta <= scaleDelta.
     */
    private val scaleDelta = 0.3f

    /**
     * Ближайшая к центру дочерняя View
     */
    val closestToCenter: View?
        get() {

            var j = -1
            var minDist = Int.MAX_VALUE

            (0 until childCount).forEach { i ->
                getChildAt(i)?.let { child ->
                    val center = childCenterY(child)
                    val dist = abs(height / 2 - center).toInt()
                    if (dist < minDist) {
                        minDist = dist
                        j = i
                    }
                }
            }

            return if (j != -1) getChildAt(j) else null
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
     * Установить alpha
     */
    protected val alphaTuner: (View, Float) -> Unit = { view, alpha ->
        view.alpha = alpha
    }

    /**
     * Установить масштаб. Здесь для нужной View устанавливается масштаб равный (1 + maxDelta).
     * То есть размер View постепенно меняется между значениями 1 и (1 + maxDelta), а maxDelta
     * меняется в диапазоне от 0 до 0.3 благодаря тому, что аргумент scale меняется от 0 до 1.
     */
    protected val scaleTuner: (View, Float) -> Unit = { view, scale ->

        val factor = scale * scaleDelta + 1f
        view.scaleX = factor
        view.scaleY = factor
    }

    /**
     * Y-координата вертикального центра дочернего элемента
     */
    private fun childCenterY(child: View) =
        (getDecoratedBottom(child) - getDecoratedTop(child)) / 2f +
                getDecoratedTop(child)
}