package s.yarlykov.izisandbox.telegram.v2

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R

class ViewHolderX (val listItem : View) : RecyclerView.ViewHolder(listItem) {

    val tv = listItem.findViewById<TextView>(R.id.tv_title)

    fun bind(position : Int) {
        val sz = "Position is $position"
        tv.text = sz
    }
}