package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.controller

import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.holder.BaseViewHolder
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.item.NoDataItem

abstract class NoDataItemController <H : BaseViewHolder> : BaseItemController<H, NoDataItem<H>> {

}