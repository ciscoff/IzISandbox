package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.sand_box.adapter_v1.base

import android.view.ViewGroup

abstract class BaseController<H : BaseHolder, I : DataItem<H>> {

    abstract fun createViewHolder(recyclerView: ViewGroup): H
    abstract fun bind(holder: H, item: I)
}