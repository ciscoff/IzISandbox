package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_02

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.layout_item_recycler_infinite.*
import s.yarlykov.izisandbox.extensions.ZDate
import s.yarlykov.izisandbox.extensions.toReadable

class ViewHolderDate (private val listItem : View) : RecyclerView.ViewHolder(listItem),
    LayoutContainer {

    override val containerView: View?
        get() = listItem

    fun bind(date : ZDate) {
        textTitle.text = date.toReadable(listItem.context)
    }
}