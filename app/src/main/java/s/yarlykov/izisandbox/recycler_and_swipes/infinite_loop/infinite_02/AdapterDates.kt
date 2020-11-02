package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_02

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.ZDate

class AdapterDates(private val model: List<ZDate>) : RecyclerView.Adapter<ViewHolderDate>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderDate {
        return ViewHolderDate(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_item_recycler_infinite,
                parent,
                false
            )
        )
    }

    override fun getItemViewType(position: Int): Int = 0

    override fun getItemCount(): Int = model.size

    override fun onBindViewHolder(holder: ViewHolderDate, position: Int) {
        holder.bind(model[position])
    }
}