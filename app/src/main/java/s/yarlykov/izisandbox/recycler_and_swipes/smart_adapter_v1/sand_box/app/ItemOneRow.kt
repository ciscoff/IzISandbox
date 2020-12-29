package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v1.sand_box.app

import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v1.controller.ItemControllerBase
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v1.model.ItemBase

@Suppress("UNCHECKED_CAST")
class ItemOneRow(controller: ControllerOneRow) :
    ItemBase<ControllerOneRow.Holder>(controller as ItemControllerBase<ControllerOneRow.Holder, ItemBase<ControllerOneRow.Holder>>) {
}