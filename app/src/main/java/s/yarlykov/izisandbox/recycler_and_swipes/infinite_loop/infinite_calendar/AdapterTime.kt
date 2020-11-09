package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R

class AdapterTime(val model: List<Int>) : RecyclerView.Adapter<ViewHolderTime>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderTime {

        return ViewHolderTime(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_item_calendar_time_picker,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = model.size

    override fun onBindViewHolder(holder: ViewHolderTime, position: Int) {
        holder.bind(model[position])
    }
}