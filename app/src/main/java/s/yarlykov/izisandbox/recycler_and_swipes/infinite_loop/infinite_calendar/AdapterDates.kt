package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.threeten.bp.LocalDate
import s.yarlykov.izisandbox.R

class AdapterDates(
    private val model: ModelDate,
    private val initialDate: LocalDate
) : RecyclerView.Adapter<ViewHolderDate>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderDate {
        return ViewHolderDate(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_item_calendar_date_picker,
                parent,
                false
            ), initialDate
        )
    }

    override fun getItemCount(): Int = model.size

    override fun onBindViewHolder(holder: ViewHolderDate, position: Int) {
        holder.bind(model[position])
    }
}