package s.yarlykov.izisandbox.recycler_and_swipes.easy_adapter

import androidx.recyclerview.widget.RecyclerView

abstract class NoDataItemController<H : RecyclerView.ViewHolder> : BaseItemController<H, NoDataItem<H>>() {

    override fun bind(holder: H, item: NoDataItem<H>) {
        // Empty, no data
    }

    override fun getItemId(item: NoDataItem<H>): Any = getTypeHashCode()
}