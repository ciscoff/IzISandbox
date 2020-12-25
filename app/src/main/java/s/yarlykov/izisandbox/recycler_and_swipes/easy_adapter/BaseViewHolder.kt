package s.yarlykov.izisandbox.recycler_and_swipes.easy_adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

open class BaseViewHolder : RecyclerView.ViewHolder {

    constructor(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : super(LayoutInflater.from(parent.context).inflate(layoutRes, parent, false))

    constructor(itemView: View) : super(itemView)

}