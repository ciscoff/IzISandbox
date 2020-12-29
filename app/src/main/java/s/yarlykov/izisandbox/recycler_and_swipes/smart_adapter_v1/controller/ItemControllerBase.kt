package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v1.controller

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v1.model.ItemBase
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v1.vh.ViewHolderBase
import kotlin.random.Random

/**
 * Отдельный тип контроллера отвечает за работу с отдельной viewType.
 * В задачи контроллера входит:
 * - создание viewHolder'а
 * - биндинг viewHolder'а с данными
 *
 * @param <H> - ViewHolder
 * @param <I> - Item
 */
abstract class ItemControllerBase<H : ViewHolderBase, I : ItemBase<H>> {

    companion object {
        const val NO_ID = RecyclerView.NO_ID
    }

    /**
     * Каждый контроллер обслуживает элементы одного viewType. Связь контроллера и viewType
     * обеспечивается HashMap'ой viewTypeIdsMap.
     *
     * key = Class<Controller>
     * value = viewType
     */
    private val viewTypeIdsMap = mutableMapOf<Class<out ItemControllerBase<in H, in I>>, Int>()

    /**
     * Биндинг viewHolder'а с элементом данных
     *
     * Изначально функция определялась как "abstract fun bind(holder: H, item: I)"
     * но это порождало ошибку компиляции типа "Out-projected type ItemControllerBase ... prohibits
     * the use of 'public open fun bind ...".
     * В общем ответ был найден по ссылке https://youtrack.jetbrains.com/issue/KT-10857.
     */
    abstract fun <H, I> bind(holder: H, item: I)

    abstract fun createViewHolder(recyclerView: ViewGroup): H

    /**
     * В качестве viewType используется hash от класса контроллера. Этот hash хранится в
     * viewTypeIdsMap, ключом является сам класс.
     */
    val viewType: Int
        get() = viewTypeHashCode()

    /**
     * Функция возвращает hash. Если при первом проходе hashMap'ы элемент не найден, то рандомно
     * генерится новый viewType, создается элемент в map'е и функция вызывается рекурсивно.
     */
    private fun viewTypeHashCode(): Int {
        var id = viewTypeIdsMap[javaClass]

        if (id == null) {
            id = Random.nextInt()
            var hasId = false

            viewTypeIdsMap.forEach { (_, v) ->
                if (id == v) {
                    hasId = true
                    return@forEach
                }
            }

            if (hasId) return viewTypeHashCode()

            viewTypeIdsMap[javaClass] = id
        }
        return id
    }
}