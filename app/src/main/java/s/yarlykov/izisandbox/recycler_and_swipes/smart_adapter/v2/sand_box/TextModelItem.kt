package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.sand_box

import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.controller.BindableItemController
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.holder.BindableViewHolder
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.model.item.BindableItem

class TextModelItem(
    data: TextModel,
    controller1: Controller1
) : BindableItem<TextModel, BindableViewHolder<TextModel>>(
    data,
    controller1 as BindableItemController<TextModel, BindableViewHolder<TextModel>>
)