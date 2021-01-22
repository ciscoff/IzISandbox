package s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.controller.BindableItemController
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.holder.BindableViewHolder
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.model.item.BindableItem
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.model.Ticket

class ColumnViewController(@LayoutRes val layoutId: Int) :
    BindableItemController<Ticket, ColumnViewController.TicketViewHolder>() {

    override fun createViewHolder(parent: ViewGroup): TicketViewHolder = TicketViewHolder(parent)

    override fun bind(holder: TicketViewHolder, item: BindableItem<Ticket, TicketViewHolder>) {
        holder.bind(item.data)
    }

    override fun viewType(): Int = layoutId

    /**
     * Пустой viewHolder
     */
    inner class TicketViewHolder(parent: ViewGroup) : BindableViewHolder<Ticket>(parent, layoutId) {

        var ticket: Ticket? = null

        override fun bind(data: Ticket) {
            ticket = data
//            itemView.setOnTouchListener(ColumnTouchListenerV1(data))
            itemView.setOnTouchListener(ColumnTouchListener(itemView, data))
        }

        override fun clear() {
            super.clear()
            itemView.isSelected = false
        }
    }
}