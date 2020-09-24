package s.yarlykov.izisandbox.recycler_and_swipes.items_animation.item_animators

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R

class ViewHolderDoodle (
    private val listItem : View,
    private val clickHandler: (Int) -> Unit) : RecyclerView.ViewHolder(listItem) {

    companion object {
        private const val multiplier = 100
    }

    val tv = listItem.findViewById<TextView>(R.id.tvDoodle)
    val iv = listItem.findViewById<ImageView>(R.id.ivDoodle)

    fun bind(position : Int) {
        tv.text = ((position + 1) * multiplier ).toString()

        listItem.setOnClickListener {
            clickHandler(position)
        }
    }

}