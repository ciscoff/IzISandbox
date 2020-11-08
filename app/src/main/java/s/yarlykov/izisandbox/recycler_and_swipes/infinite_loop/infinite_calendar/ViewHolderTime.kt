package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_calendar

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ViewHolderTime(val listItem : View) : RecyclerView.ViewHolder(listItem) {
    fun bind (timeUnit : Int) {
        listItem.tag = timeUnit
        (listItem as TextView).text = "%02d".format(timeUnit)
    }
}