package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.model

import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.base.DataItem

class ItemList : ArrayList<DataItem>() {

    companion object {
        fun create() = ItemList()
    }

    fun addItem(item: DataItem): ItemList {
        return insert(this.size, item)
    }

    fun insert(index: Int, item: DataItem): ItemList {
        this.add(index, item)
        return this
    }

}