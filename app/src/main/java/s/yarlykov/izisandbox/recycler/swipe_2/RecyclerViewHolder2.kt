package s.yarlykov.izisandbox.recycler.swipe_2

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R

class RecyclerViewHolder2(
    private val listItem: View,
    private val callback: (Int) -> Unit
) : RecyclerView.ViewHolder(listItem) {

    private val textView = listItem.findViewById<TextView>(R.id.tv_title)
    private val upperLayer = listItem.findViewById<LinearLayout>(R.id.upper_layer)
    private val buttonDelete1 = listItem.findViewById<ImageView>(R.id.delete_row_1)
    private val buttonDelete2 = listItem.findViewById<ImageView>(R.id.delete_row_2)

    fun bind(data: String) {
        textView.text = data

        upperLayer.setOnTouchListener(DragGestureToLeftHandler(upperLayer))

        buttonDelete1.setOnClickListener {
            callback(adapterPosition)
            upperLayer.translationX = 0f
        }
        buttonDelete2.setOnClickListener {
            callback(adapterPosition)
            upperLayer.translationX = 0f
        }
    }
}
