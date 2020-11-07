package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_calendar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_infinite_date_picker.*
import s.yarlykov.izisandbox.R

class ScrollingCalendar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val listHours: RecyclerView
    private val listDates: RecyclerView
    private val listMinutes: RecyclerView

    private val modelHours = (0 until 24).toList()
    private val modelMinutes = (0 until 60).toList()
    private val modelDates = ModelDate()

    init {
        View.inflate(context, R.layout.layout_scrolling_calendar, this).also {
            listDates = it.findViewById<RecyclerView>(R.id.listDates).apply {
                layoutManager = InfiniteLayoutManager(modelDates)
                adapter = AdapterDates(modelDates)
            }
            listHours = it.findViewById<RecyclerView>(R.id.listHours).apply {
                layoutManager = LoopLayoutManager()
                adapter = AdapterTime(modelHours)
            }
            listMinutes = it.findViewById<RecyclerView>(R.id.listMinutes).apply {
                layoutManager = LoopLayoutManager()
                adapter = AdapterTime(modelMinutes)
            }

            LinearSnapHelper().apply {
                attachToRecyclerView(listDates)
            }

            LinearSnapHelper().apply {
                attachToRecyclerView(listHours)
            }

            LinearSnapHelper().apply {
                attachToRecyclerView(listMinutes)
            }
        }

        isSaveEnabled = true
    }
}