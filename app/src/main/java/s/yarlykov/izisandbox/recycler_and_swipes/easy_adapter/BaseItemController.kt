package s.yarlykov.izisandbox.recycler_and_swipes.easy_adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.util.*

/**
 * H - тип ViewHolder'a
 * I - тип Item'a
 */
abstract class BaseItemController<H : RecyclerView.ViewHolder, I : BaseItem<H>> {

    /**
     * Карта viewType'ов
     */
    private val viewTypeIdsMap = mutableMapOf<Class<BaseItemController<H, I>>, Int>()

    /**
     * Привязать ViewHolder к контроллеру
     */
    abstract fun bind(holder: H, item: I)

    abstract fun createViewHolder(parent: ViewGroup): H

    abstract fun getItemId(item: I): Any

    open fun viewType(): Int = getTypeHashCode()

    /**
     * Контроллер является дженериком, поэтому конкретные типы получаются только после того
     * как определены его type-arguments. После того как определен конкретный тип, то для него
     * генерится уникальное Int (бля, перебором) и хранится в мапе как viewType.
     *
     * TODO Надо как-то причесать код. Тут он рекурсивно вызывается пока не сгенерится
     * TODO уникальный Int.
     */
    open fun getTypeHashCode(): Int {
        val clazz: Class<BaseItemController<H, I>> = javaClass

        return viewTypeIdsMap[clazz] ?: run {
            val id = Random().nextInt()
            var hasId = false

            loop@ for ((_, value) in viewTypeIdsMap.entries) {
                if (id == value) {
                    hasId = true
                    break@loop
                }
            }
            if (hasId) {
                return getTypeHashCode()
            }

            viewTypeIdsMap[clazz] = id
            id
        }
    }
}