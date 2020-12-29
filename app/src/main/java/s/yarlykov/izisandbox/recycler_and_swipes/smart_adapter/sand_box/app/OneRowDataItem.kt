package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.sand_box.app

import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.base.BaseDataItem
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.fabric.ViewHolderFabric

class OneRowDataItem : BaseDataItem() {

    init {
        ViewHolderFabric.registerViewHolder(viewType, OneRowViewHolder.Create::action)
    }

}