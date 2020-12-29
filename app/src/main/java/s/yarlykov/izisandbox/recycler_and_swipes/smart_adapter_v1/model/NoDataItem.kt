package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v1.model

import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v1.controller.ItemControllerBase
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v1.controller.NoDataItemController
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v1.vh.ViewHolderBase

@Suppress("UNCHECKED_CAST")
class NoDataItem<H : ViewHolderBase>(itemController: NoDataItemController<H>) :
    ItemBase<H>(itemController as ItemControllerBase<H, ItemBase<H>>) {
}