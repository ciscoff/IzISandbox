package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v1.sand_box.app

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v1.controller.ItemControllerBase
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v1.vh.ViewHolderBase

class ControllerTwoRows(@LayoutRes layoutId: Int) :
    ItemControllerBase<ControllerTwoRows.Holder, ItemTwoRows>() {

    override fun <H, I> bind(holder: H, item: I) {

    }

    override fun createViewHolder(recyclerView: ViewGroup): ControllerTwoRows.Holder {
        TODO("Not yet implemented")
    }

    class Holder(recyclerView: ViewGroup, layoutId: Int) : ViewHolderBase(recyclerView, layoutId)
}