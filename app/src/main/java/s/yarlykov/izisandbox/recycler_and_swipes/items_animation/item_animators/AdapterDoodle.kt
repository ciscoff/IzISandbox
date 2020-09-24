package s.yarlykov.izisandbox.recycler_and_swipes.items_animation.item_animators

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R

class AdapterDoodle(
    private val count: Int,
    private val clickHandler: (Int) -> Unit
) : RecyclerView.Adapter<ViewHolderDoodle>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderDoodle {
        return ViewHolderDoodle(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_item_recycler_6,
                parent,
                false
            ), clickHandler
        )
    }

    override fun getItemCount(): Int = count

    override fun onBindViewHolder(holder: ViewHolderDoodle, position: Int) {
        holder.bind(position)
    }
}