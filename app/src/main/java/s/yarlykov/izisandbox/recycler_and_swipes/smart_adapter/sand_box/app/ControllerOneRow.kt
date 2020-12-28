package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.sand_box.app

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.controller.ItemControllerBase
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.vh.ViewHolderBase


class ControllerOneRow(@LayoutRes layoutId: Int) : ItemControllerBase<ControllerOneRow.Holder, ItemOneRow>() {

    override fun <H, I> bind(holder: H, item: I) {
        TODO("Not yet implemented")
    }

    override fun createViewHolder(recyclerView: ViewGroup): Holder {
        TODO("Not yet implemented")
    }

    class Holder(recyclerView: ViewGroup, layoutId: Int) : ViewHolderBase(recyclerView, layoutId)
}