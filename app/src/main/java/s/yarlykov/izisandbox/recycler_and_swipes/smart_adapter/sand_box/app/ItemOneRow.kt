package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.sand_box.app

import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.controller.ItemControllerBase
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.model.ItemBase

@Suppress("UNCHECKED_CAST")
class ItemOneRow(controller: ControllerOneRow) :
    ItemBase<ControllerOneRow.Holder>(controller as ItemControllerBase<ControllerOneRow.Holder, ItemBase<ControllerOneRow.Holder>>) {
}