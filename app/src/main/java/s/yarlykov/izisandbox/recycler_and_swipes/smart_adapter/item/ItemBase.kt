package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.item

import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.controller.ItemControllerBase

open class ItemBase<H : RecyclerView.ViewHolder>(
    val itemController: ItemControllerBase<H, ItemBase<H>>
) {
    var nextItem: ItemBase<H>? = null
    var prevItem: ItemBase<H>? = null

    var position: Int? = null
    var adapterPosition: Int? = null
}