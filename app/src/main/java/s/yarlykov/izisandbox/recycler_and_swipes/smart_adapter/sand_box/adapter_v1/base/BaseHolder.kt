package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.sand_box.adapter_v1.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

abstract class BaseHolder : RecyclerView.ViewHolder {

    constructor(recyclerView: ViewGroup, @LayoutRes layoutId: Int)
            : super(
        LayoutInflater.from(recyclerView.context).inflate(layoutId, recyclerView, false)
    )

    constructor(itemView: View) : super(itemView)
}