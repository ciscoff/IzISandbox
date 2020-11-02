package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_01

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R

class AdapterInfinite : RecyclerView.Adapter<ViewHolderInfinite>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderInfinite {
        return ViewHolderInfinite(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_item_recycler_infinite,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = 20

    override fun onBindViewHolder(holder: ViewHolderInfinite, position: Int) {
        holder.bind(position)
    }
}