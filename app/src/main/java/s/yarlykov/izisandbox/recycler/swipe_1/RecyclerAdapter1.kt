package s.yarlykov.izisandbox.recycler.swipe_1

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R

class RecyclerAdapter1(private val model: MutableList<String>) : RecyclerView.Adapter<RecyclerViewHolder1>(),
    ItemTouchHelperAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder1 {

        return RecyclerViewHolder1(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_item_recycler_1,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = model.size

    override fun onBindViewHolder(holder: RecyclerViewHolder1, position: Int) {
        holder.bind(model[position])

    }

    override fun onItemDismiss(position: Int) {
        model.removeAt(position)
        notifyItemRemoved(position)

    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {

    }
}