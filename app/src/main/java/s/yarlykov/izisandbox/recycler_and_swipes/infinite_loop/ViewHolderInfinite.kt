package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.layout_item_recycler_infinite.*
import s.yarlykov.izisandbox.R

class ViewHolderInfinite(private val listItem: View) : RecyclerView.ViewHolder(listItem),
    LayoutContainer {

    override val containerView: View?
        get() = listItem

    fun bind(position: Int) {
        textTitle.text = listItem.context.getString(R.string.adapter_position, position)
    }
}