package s.yarlykov.izisandbox.recycler.swipe

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R

class RecyclerAdapter(private val model: MutableList<String>) : RecyclerView.Adapter<RecyclerViewHolder>(),
    ItemTouchHelperAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        return RecyclerViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_item_recycler,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = model.size

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.bind(model[position])

    }

    override fun onItemDismiss(position: Int) {
        model.removeAt(position)
        notifyItemRemoved(position)

    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {

    }
}