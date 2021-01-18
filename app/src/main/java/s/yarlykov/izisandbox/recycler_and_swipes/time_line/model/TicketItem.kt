package s.yarlykov.izisandbox.recycler_and_swipes.time_line.model

import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.model.item.BindableItem
import s.yarlykov.izisandbox.recycler_and_swipes.time_line.ColumnViewController

class TicketItem(
    ticket: Ticket,
    controller: ColumnViewController
) : BindableItem<Ticket, ColumnViewController.TicketViewHolder>(ticket, controller)
