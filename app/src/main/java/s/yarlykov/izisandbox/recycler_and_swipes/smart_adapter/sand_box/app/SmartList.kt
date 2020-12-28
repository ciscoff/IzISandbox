package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.sand_box.app

import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.model.ItemBase
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.vh.ViewHolderBase

class SmartList : ArrayList<ItemBase<ViewHolderBase>>() {

    companion object {
        fun create() = SmartList()
    }

    fun <H : ViewHolderBase> addItem(item: ItemBase<H>): SmartList {
        return insert(this.size, item)
    }

    @Suppress("UNCHECKED_CAST")
    fun <H : ViewHolderBase> insert(index: Int, item: ItemBase<H>): SmartList {
        this.add(index, item as ItemBase<ViewHolderBase>)
        return this
    }
}