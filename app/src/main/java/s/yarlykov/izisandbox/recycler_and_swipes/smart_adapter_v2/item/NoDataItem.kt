package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.item


import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.controller.BaseItemController
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.holder.BaseViewHolder

@Suppress("UNCHECKED_CAST")
class NoDataItem<H : BaseViewHolder>(controller: BaseItemController<H, NoDataItem<H>>) :
    BaseItem<H>(controller as BaseItemController<H, BaseItem<H>>)