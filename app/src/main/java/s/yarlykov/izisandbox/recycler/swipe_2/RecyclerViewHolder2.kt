package s.yarlykov.izisandbox.recycler.swipe_1

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.Utils.logIt

class RecyclerViewHolder1 (private val listItem: View) : RecyclerView.ViewHolder(listItem) {

    private val textView = listItem.findViewById<TextView>(R.id.tv_title)

    init {
        listItem.setOnClickListener {
            logIt("I'm was clicked")
        }


    }

    fun bind(data : String) {
        textView.text = data
    }
}
