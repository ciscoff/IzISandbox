package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.model

import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.controller.ItemControllerBase
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.vh.ViewHolderBase

/**
 * Subtyping:
 * ItemBase работает "какбэ" со стандартным ViewHolder'ом
 */
open class ItemBase<H : ViewHolderBase>(
    val itemController: ItemControllerBase<H, ItemBase<H>>
) {
    var nextItem: ItemBase<H>? = null
    var prevItem: ItemBase<H>? = null

    var position: Int? = null
    var adapterPosition: Int? = null
}