package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_02

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.layout_item_recycler_infinite.*
import org.threeten.bp.LocalDate
import org.threeten.bp.Period
import s.yarlykov.izisandbox.Utils.logIt
import s.yarlykov.izisandbox.extensions.toReadable

/**
 * Полезная статья о расчетах разницы в датах/времени
 * https://mkyong.com/java8/java-8-difference-between-two-localdate-or-localdatetime/
 */
class ViewHolderDate (private val listItem : View) : RecyclerView.ViewHolder(listItem),
    LayoutContainer {

    override val containerView: View?
        get() = listItem

    /**
     * В тег записываем количество дней, на которое дата в элементе отличается от сегодняшней.
     */
    fun bind(date : LocalDate) {
        val period = Period.between(LocalDate.now(), date)

        val tmpDays = period.days

        listItem.tag = period.days
        textTitle.text = date.toReadable(listItem.context)

        logIt("ViewHolder::bind ${date.toReadable(listItem.context)}", "PLPLPL")
    }
}