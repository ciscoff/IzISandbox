package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.item


import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.controller.BaseController
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.controller.NoDataItemController
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.holder.BaseViewHolder

@Suppress("UNCHECKED_CAST")
class NoDataItem<H : BaseViewHolder>(controller: NoDataItemController<H>) :
    BaseItem<H>(controller as BaseController<H, BaseItem<H>>)