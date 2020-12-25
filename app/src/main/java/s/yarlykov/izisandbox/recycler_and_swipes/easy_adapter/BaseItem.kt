package s.yarlykov.izisandbox.recycler_and_swipes.easy_adapter

import androidx.recyclerview.widget.RecyclerView

/**
 * Смысл дженерика в том, что и BaseItem и контроллер будут работать с ViewHolder'ом
 * который заточен на один viewType. То еть не получится так, что почему-то viewType
 * не совпал.
 *
 * То есть: ViewHolder - это viewType и он должен быть одинаков у Item'a и его контроллера.
 */
open class BaseItem<H : RecyclerView.ViewHolder>(val itemController: BaseItemController<H, BaseItem<H>>) {

    var nextItem : BaseItem<H>? = null
    var previousItem: BaseItem<H>? = null

    var position: Int = 0
    var adapterPosition : Int = 0
}