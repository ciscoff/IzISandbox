package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v1.sand_box.app

import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v1.base.BaseDataItem
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v1.fabric.ViewHolderFabric

class OneRowDataItem : BaseDataItem() {

    init {
        ViewHolderFabric.registerViewHolder(viewType, OneRowViewHolder.Create::action)
    }

}