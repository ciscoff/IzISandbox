package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.model

import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.controller.BindableItemController
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.controller.NoDataItemController
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.holder.BindableViewHolder
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.item.BaseItem
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.item.BindableItem
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.item.NoDataItem

/**
 * Это фактически использования Java Raw Types
 */
typealias ListItem = BaseItem<*>

class SmartList : ArrayList<ListItem>() {

    companion object {
        fun create() = SmartList()
    }

    /**
     * Добавить элемент, который не содержит данных
     */
    fun addItem(controller : NoDataItemController<*>) {
        insert(size, NoDataItem(controller))
    }

    /**
     * Добавить элемент с данными.
     */
    fun <T : Any, H : BindableViewHolder<T>> addItem(
        data: T,
        controller: BindableItemController<T, H>
    ) {
        insert(size, BindableItem(data, controller))
        val a = 1
    }

    private fun insert(index: Int, item: ListItem): SmartList {
        this.add(index, item)
        return this
    }

}