package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.model

import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.holder.BaseViewHolder
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.item.BaseItem

typealias ListItem = BaseItem<BaseViewHolder>

class SmartList : ArrayList<ListItem>() {

    companion object {
        fun create() = SmartList()
    }

    fun addItem(item: ListItem): SmartList {
        return insert(this.size, item)
    }

    fun insert(index: Int, item: ListItem): SmartList {
        this.add(index, item)
        return this
    }

}