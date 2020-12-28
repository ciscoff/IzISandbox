package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.vh

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

/**
 * Базывый ViewHolder
 */
abstract class ViewHolderBase : RecyclerView.ViewHolder {
    constructor(recyclerView: ViewGroup, @LayoutRes layoutId: Int)
            : super(
        LayoutInflater.from(recyclerView.context).inflate(layoutId, recyclerView, false)
    )

    constructor(itemView: View) : super(itemView)
}