package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.item

import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.controller.ItemControllerBase
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.controller.NoDataItemController

class NoDataItem<H : RecyclerView.ViewHolder>(
    itemController: NoDataItemController<H>
) : ItemBase<H>(itemController as ItemControllerBase<H, ItemBase<H>>) {

}