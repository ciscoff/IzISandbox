package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.model

import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.controller.ItemControllerBase
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.controller.NoDataItemController
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.vh.ViewHolderBase

@Suppress("UNCHECKED_CAST")
class NoDataItem<H : ViewHolderBase>(itemController: NoDataItemController<H>) :
    ItemBase<H>(itemController as ItemControllerBase<H, ItemBase<H>>) {
}