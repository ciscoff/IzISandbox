package s.yarlykov.izisandbox.recycler_and_swipes.swipe_4

import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_4.animation.ItemDragHandlerV4

class ViewHolderItem (private val listItem : View) : RecyclerView.ViewHolder(listItem) {

    private val underLayer = listItem.findViewById<FrameLayout>(R.id.under_layer_4)
    private val upperLayer = listItem.findViewById<FrameLayout>(R.id.upper_layer_4)

    fun bind() {
        upperLayer.setOnTouchListener(ItemDragHandlerV4())
    }
}