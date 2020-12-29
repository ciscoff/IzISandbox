package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2

import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.base.BaseDataItem
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.fabric.ViewHolderFabric

class TwoRowsDataItem : BaseDataItem(TwoRowsViewHolder::class.java) {

    init {
        ViewHolderFabric.registerViewHolder(viewType, TwoRowsViewHolder.Create::action)
    }

}