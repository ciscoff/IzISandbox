package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.sand_box.app

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.controller.ItemControllerBase
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.vh.ViewHolderBase

class ControllerTwoRows(@LayoutRes layoutId: Int) :
    ItemControllerBase<ControllerTwoRows.Holder, ItemTwoRows>() {

    override fun <H, I> bind(holder: H, item: I) {

    }

    override fun createViewHolder(recyclerView: ViewGroup): ControllerTwoRows.Holder {
        TODO("Not yet implemented")
    }

    class Holder(recyclerView: ViewGroup, layoutId: Int) : ViewHolderBase(recyclerView, layoutId)
}