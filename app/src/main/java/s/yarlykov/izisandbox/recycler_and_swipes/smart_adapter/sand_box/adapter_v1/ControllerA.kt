package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.sand_box.adapter_v1

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.sand_box.adapter_v1.base.BaseController

class ControllerA(@LayoutRes val layoutId: Int) : BaseController<HolderA, DataItemA>() {

    override fun createViewHolder(recyclerView: ViewGroup): HolderA {
        return HolderA(recyclerView, layoutId)
    }

    override fun bind(holder: HolderA, item: DataItemA) {
        // no data
    }
}