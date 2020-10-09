package s.yarlykov.izisandbox.recycler_and_swipes.items_animation.layout_animation

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.GridLayoutAnimationController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.Utils.logIt

/**
 * Если View анимируется с помощью LayoutAnimation (а точнее с помощью layout animation controller),
 * то ее LayoutParams получают дополнительные параметры, относящиеся к этой анимации. Эти
 * дополнительные параметры упакованы в инстанс LayoutAnimationController.AnimationParameters,
 * который содержит всего два Int-поля: count и index.
 *   index - это индекс данного чайлда внутри родителя
 *   count - это количество чайлдов у родителя.
 * Благодаря этим значениям расчитывается start time анимации конкретного View.
 * NOTE: index подразумевает сквозную индексацию детей, то есть как бы "линейно". Однако,
 * если нужно оценить порядок детей с точки зрения "строка/столбец", то нужен иной подход.
 * В этом случае используется GridLayoutAnimationController.AnimationParameters, у которого
 * есть дополнительно columnsCount и rowsCount и об этом ниже.
 *
 * Так вот у ViewGroup есть метод attachLayoutAnimationParameters чтобы приаттачить параметры
 * анимации к дочерним View. RecyclerView является ViewGroup, НО по умолчанию он считает, что его
 * дети расположены линейно и использует LayoutAnimationController.AnimationParameters. Мы же хотим
 * увидеть на экране Grid, поэтому переходим на GridLayoutAnimationController.AnimationParameters.
 */

class GridRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    /**
     * Задача следующая: на входе имеем index дочерней View и count - количество ВИДИМЫХ на экране
     * дочерних элементов (похоже, что layout уже выполнен раз известно кол-во видимых элементов).
     * Нужно превратить линейный index в двумерный column/row и записать результат в
     * GridLayoutAnimationController.AnimationParameters.
     *
     * NOTE: Здесь расчет column/row выполняется через invertedIndex и получается криво, если
     * количество видимых элементов не закрывают полностью экран. Нужно по другому сделать.
     */
    override fun attachLayoutAnimationParameters(
        child: View,
        params: ViewGroup.LayoutParams,
        index: Int,
        count: Int
    ) {
        val (adapter, layoutManager) = adapter to layoutManager

        if (adapter != null && layoutManager is GridLayoutManager) {

            val animationParams =
                params.layoutAnimationParameters
                        as? GridLayoutAnimationController.AnimationParameters
                    ?: GridLayoutAnimationController.AnimationParameters()

            params.layoutAnimationParameters = animationParams

            val columns = layoutManager.spanCount

            animationParams.count = count
            animationParams.index = index
            animationParams.columnsCount = columns
            animationParams.rowsCount = count / columns

            val invertedIndex = count - 1 - index

            animationParams.column = columns - 1 - (invertedIndex % columns)
            animationParams.row = animationParams.rowsCount - 1 - invertedIndex / columns

            animationParams.apply {
                logIt("count=$count, index=$index, invertedIndex=$invertedIndex, column=$column, row=$row")
            }

        } else {
            super.attachLayoutAnimationParameters(child, params, index, count)
        }
    }
}