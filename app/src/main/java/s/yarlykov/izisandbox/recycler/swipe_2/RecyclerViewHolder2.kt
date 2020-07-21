package s.yarlykov.izisandbox.recycler.swipe_2

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.Utils.logIt

class RecyclerViewHolder2(
    private val listItem: View,
    private val callback: (Int) -> Unit
) : RecyclerView.ViewHolder(listItem) {

    private val textView = listItem.findViewById<TextView>(R.id.tv_title)
    private val upperLayer = listItem.findViewById<LinearLayout>(R.id.upper_layer)
    private val buttonDelete = listItem.findViewById<ImageView>(R.id.delete_row)

    fun bind(data: String) {
        textView.text = data

        upperLayer.setOnTouchListener(DragGestureHandler(upperLayer))
        buttonDelete.setOnClickListener {
            callback(adapterPosition)
        }

    }
}
