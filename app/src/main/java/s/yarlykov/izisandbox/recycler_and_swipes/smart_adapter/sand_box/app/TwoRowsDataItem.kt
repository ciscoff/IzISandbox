package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.sand_box.app

import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.base.BaseDataItem
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.fabric.ViewHolderFabric

class TwoRowsDataItem : BaseDataItem() {

    init {
        ViewHolderFabric.registerViewHolder(viewType, TwoRowsViewHolder.Create::action)
    }
}