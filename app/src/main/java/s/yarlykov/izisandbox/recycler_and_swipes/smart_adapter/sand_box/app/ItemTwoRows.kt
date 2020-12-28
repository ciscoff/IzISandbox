package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.sand_box.app

import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.controller.ItemControllerBase
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.model.ItemBase
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.vh.ViewHolderBase

@Suppress("UNCHECKED_CAST")
class ItemTwoRows(controller: ControllerTwoRows) :
    ItemBase<ControllerTwoRows.Holder>(controller as ItemControllerBase<ControllerTwoRows.Holder, ItemBase<ControllerTwoRows.Holder>>)