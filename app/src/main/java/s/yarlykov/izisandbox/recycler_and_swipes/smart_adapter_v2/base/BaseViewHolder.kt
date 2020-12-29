package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder : RecyclerView.ViewHolder {

    constructor(recyclerView: ViewGroup, @LayoutRes layoutId: Int) : super(
        LayoutInflater.from(recyclerView.context)
            .inflate(
                layoutId,
                recyclerView,
                false
            )
    )

    constructor(itemView: View) : super(itemView)

    abstract fun bind(item: DataItem)
}