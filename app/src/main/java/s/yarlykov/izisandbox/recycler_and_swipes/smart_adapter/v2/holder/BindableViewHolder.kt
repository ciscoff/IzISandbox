package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.holder

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

abstract class BindableViewHolder<in T> : BaseViewHolder {

    constructor(recyclerView: ViewGroup, @LayoutRes layoutId: Int) : super(recyclerView, layoutId)

    constructor(itemView: View) : super(itemView)

    abstract fun bind(data: T)
}