package s.yarlykov.izisandbox.recycler_and_swipes.swipe_4

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R

private const val itemsQty = 15

class AdapterItem : RecyclerView.Adapter<ViewHolderItem>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderItem {
        return ViewHolderItem(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_item_recycler_4,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = itemsQty

    override fun onBindViewHolder(holder: ViewHolderItem, position: Int) {
        holder.bind()
    }
}