package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v1.model


import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v1.controller.NoDataItemController
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v1.vh.ViewHolderBase

class ItemList : ArrayList<ItemBase<*>>() {

    companion object {
        fun create() = ItemList()
    }

    fun <H : ViewHolderBase> add(itemController: NoDataItemController<H>) {
        addItem(NoDataItem(itemController))
    }

    fun <H : ViewHolderBase> addItem(item: ItemBase<H>): ItemList {
        return insert(this.size, item)
    }

    fun <H : ViewHolderBase> insert(index: Int, item: ItemBase<H>): ItemList {
        this.add(index, item)
        return this
    }
}