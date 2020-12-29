package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v1.sand_box.app

import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v1.controller.ItemControllerBase
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v1.model.ItemBase

@Suppress("UNCHECKED_CAST")
class ItemTwoRows(controller: ControllerTwoRows) :
    ItemBase<ControllerTwoRows.Holder>(controller as ItemControllerBase<ControllerTwoRows.Holder, ItemBase<ControllerTwoRows.Holder>>)