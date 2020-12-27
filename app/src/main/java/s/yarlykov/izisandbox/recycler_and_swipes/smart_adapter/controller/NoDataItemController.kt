package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.controller

import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.item.NoDataItem

abstract class NoDataItemController<H : RecyclerView.ViewHolder>
    : ItemControllerBase<H, NoDataItem<H>>() {

    override fun bind(holder: H, item: NoDataItem<H>) {
        // empty. no data
    }

}