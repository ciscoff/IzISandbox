package s.yarlykov.izisandbox.recycler_and_swipes.swipe_2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R

class RecyclerAdapter2(private val model: MutableList<String>) :
    RecyclerView.Adapter<RecyclerViewHolder2>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder2 {

        return RecyclerViewHolder2(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_item_recycler_2,
                parent,
                false
            ), ::onDeleteEventHandler
        )
    }

    override fun getItemCount(): Int = model.size

    override fun onBindViewHolder(holder: RecyclerViewHolder2, position: Int) {
        holder.bind(model[position])
    }

    private fun onDeleteEventHandler(position: Int) {
        model.removeAt(position)
        notifyItemRemoved(position)
    }
}