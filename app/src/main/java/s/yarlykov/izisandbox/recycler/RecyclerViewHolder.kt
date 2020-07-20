package s.yarlykov.izisandbox.recycler

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R

class RecyclerViewHolder (private val listItem: View) : RecyclerView.ViewHolder(listItem) {

    private val textView = listItem.findViewById<TextView>(R.id.tv_title)

    fun bind(data : String) {
        textView.text = data
    }
}
