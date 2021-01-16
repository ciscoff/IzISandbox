package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder : RecyclerView.ViewHolder {

    constructor(parent: ViewGroup, @LayoutRes layoutId: Int) : super(
        LayoutInflater.from(parent.context)
            .inflate(
                layoutId,
                parent,
                false
            )
    )

    constructor(itemView: View) : super(itemView)
}