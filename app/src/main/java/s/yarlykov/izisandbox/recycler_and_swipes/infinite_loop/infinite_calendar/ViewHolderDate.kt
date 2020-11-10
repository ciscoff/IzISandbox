package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_calendar

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import org.threeten.bp.Duration
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import s.yarlykov.izisandbox.extensions.toReadable

class ViewHolderDate(
    private val listItem: View,
    private val initialDate: LocalDate) : RecyclerView.ViewHolder(listItem),
    LayoutContainer {

    override val containerView: View?
        get() = listItem

    /**
     * В тег записываем количество дней, на которое дата в элементе отличается от initialDay.
     * Это значение со знаком, то есть учитывается смещение вперед/назад от initialDay.
     */
    fun bind(date: LocalDate) {
        val initialDay = LocalDateTime.of(initialDate, LocalTime.MIDNIGHT)
        val someDay = LocalDateTime.of(date, LocalTime.MIDNIGHT)

        val period = Duration.between(initialDay, someDay).toDays()
        listItem.tag = period.toInt()
        (listItem as TextView).text = date.toReadable(listItem.context)
    }
}