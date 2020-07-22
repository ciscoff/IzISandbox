package s.yarlykov.izisandbox.recycler.swipe_with_undo

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R

/**
 * ViewHolder capable of presenting two states: "normal" and "undo" state.
 */
class TestViewHolder(private val listItem: View) : RecyclerView.ViewHolder(listItem) {

    val titleTextView: TextView  = itemView.findViewById(R.id.title_text_view)
    var undoButton: Button = itemView.findViewById(R.id.undo_button)
}