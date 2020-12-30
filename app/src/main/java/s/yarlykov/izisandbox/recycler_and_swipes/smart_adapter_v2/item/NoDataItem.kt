package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.item


import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.controller.BaseItemController
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.controller.NoDataItemController
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.holder.BaseViewHolder

@Suppress("UNCHECKED_CAST")
// BaseItemController<H, BaseItem<H>>
// BaseItemController<H, NoDataItem<H>>

//class BaseItem<H : BaseViewHolder>(val controller: BaseItemController<H, BaseItem<H>>)

//BaseItemController<H : BaseViewHolder, I : BaseItem<H>>
//NoDataItemController <H : BaseViewHolder> : BaseItemController<H, NoDataItem<H>>


class NoDataItem<H : BaseViewHolder>(controller: NoDataItemController<H>) :
    BaseItem<H>(controller as BaseItemController<H, BaseItem<H>>)