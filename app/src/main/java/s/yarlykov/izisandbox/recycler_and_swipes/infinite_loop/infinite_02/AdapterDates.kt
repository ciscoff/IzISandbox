package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_02

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.ZDate
import s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_02.InfiniteDatePickerActivity.Companion.MODEL_SIZE

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

    override fun getItemCount(): Int = MODEL_SIZE

    override fun onBindViewHolder(holder: ViewHolderDate, position: Int) {
        holder.bind(model[position])
    }
}