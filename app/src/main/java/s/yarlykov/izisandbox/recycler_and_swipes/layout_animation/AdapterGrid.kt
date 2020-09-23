package s.yarlykov.izisandbox.recycler_and_swipes.layout_animation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R

class AdapterGrid(private val count : Int) : RecyclerView.Adapter<ViewHolderX>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderX {
        return ViewHolderX(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_item_recycler_5,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = count

    override fun onBindViewHolder(holder: ViewHolderX, position: Int) {
    }
}