package s.yarlykov.izisandbox.recycler_and_swipes.grid

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R

class AdapterCell(val model : Int) : RecyclerView.Adapter<ViewHolderCell>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderCell {
        return ViewHolderCell(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_grid_cell,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolderCell, position: Int) {
    }

    override fun getItemCount(): Int = model
}