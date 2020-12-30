package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.model

import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.controller.BindableItemController
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.holder.BaseViewHolder
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.holder.BindableViewHolder
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.item.BaseItem
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.item.BindableItem

/**
 * Это фактически использования Java Raw Types
 */
typealias ListItem = BaseItem<*>

class SmartList : ArrayList<ListItem>() {

    companion object {
        fun create() = SmartList()
    }

    fun <T : Any, H : BindableViewHolder<T>> addBindable(
        data: T,
        controller: BindableItemController<T, H>
    ) {
        insert(size, BindableItem(data, controller))
        val a = 1
    }

    fun addItem(item: ListItem): SmartList {
        return insert(this.size, item)
    }

    fun insert(index: Int, item: ListItem): SmartList {
        this.add(index, item)
        return this
    }

}