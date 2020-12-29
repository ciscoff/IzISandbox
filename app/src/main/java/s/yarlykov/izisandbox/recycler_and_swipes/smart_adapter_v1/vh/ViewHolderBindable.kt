package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v1.vh

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

/**
 * Это прототип для создания всех остальных ViewHolder'ов
 */
abstract class ViewHolderBindable<T> : ViewHolderBase {

    constructor(recyclerView: ViewGroup, @LayoutRes layoutId: Int) : super(recyclerView, layoutId)

    constructor(itemView: View) : super(itemView)

    abstract fun bind(data: T)
}