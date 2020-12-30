package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.sand_box

import android.view.ViewGroup
import android.widget.TextView
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.holder.BindableViewHolder

class ViewHolder2(parent: ViewGroup, layoutRes: Int) :
    BindableViewHolder<TextModel>(parent, layoutRes) {

    private val textTitle = itemView.findViewById<TextView>(R.id.textTitle)
    private val textDescription = itemView.findViewById<TextView>(R.id.textDescription)

    override fun bind(data: TextModel) {
        textTitle?.apply { text = data.header }
        textDescription?.apply { text = data.description }
    }
}