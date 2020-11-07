package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_calendar

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.layout_item_recycler_infinite_date_picker.*
import org.threeten.bp.Duration
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import s.yarlykov.izisandbox.extensions.toReadable

class ViewHolderDate(private val listItem: View) : RecyclerView.ViewHolder(listItem),
    LayoutContainer {

    override val containerView: View?
        get() = listItem

    /**
     * В тег записываем количество дней, на которое дата в элементе отличается от сегодняшней.
     * Это значение со знаком, то есть учитывается смещение вперед/назад от сегодня.
     */
    fun bind(date: LocalDate) {
        val today = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT)
        val someDay = LocalDateTime.of(date, LocalTime.MIDNIGHT)

        val period = Duration.between(today, someDay).toDays()
        listItem.tag = period.toInt()
        (listItem as TextView).text = date.toReadable(listItem.context)
    }
}