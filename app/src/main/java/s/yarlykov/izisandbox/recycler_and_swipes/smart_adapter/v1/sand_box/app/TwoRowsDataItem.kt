package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v1.sand_box.app

import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v1.base.BaseDataItem
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v1.fabric.ViewHolderFabric

class TwoRowsDataItem : BaseDataItem() {

    init {
        ViewHolderFabric.registerViewHolder(viewType, TwoRowsViewHolder.Create::action)
    }
}