package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.controller

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.vh.ViewHolderBase

class Controller(@LayoutRes val layoutId : Int) : NoDataItemController<Controller.Holder>() {

    override fun createViewHolder(recyclerView: ViewGroup) : Holder {
        return Holder(recyclerView, layoutId)
    }

    class Holder(recyclerView : ViewGroup, layoutId : Int) : ViewHolderBase(recyclerView, layoutId)
}