package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.sand_box.adapter_v1.base

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

abstract class BaseHolderBindble <T> : BaseHolder {

    constructor(recyclerView: ViewGroup, @LayoutRes layoutId: Int) : super(recyclerView, layoutId)

    constructor(itemView: View) : super(itemView)

    abstract fun bind (data : T)
}