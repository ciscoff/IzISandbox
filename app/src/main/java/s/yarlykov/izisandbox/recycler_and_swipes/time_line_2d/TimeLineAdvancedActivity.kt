package s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_time_line_advanced.*
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.Decorator
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.adapter.SmartAdapterV2
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.model.SmartList
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.decors.ColumnOffsetDecor
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.decors.DutyTimeDecor
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.decors.BarsDecor
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.model.Ticket
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.model.TicketItem

class TimeLineAdvancedActivity : AppCompatActivity() {

    companion object {
        const val DAY_START = 9
        const val DAY_END = 21
    }

    private val smartAdapter = SmartAdapterV2()

    private val tickets = listOf(
        Ticket("Ticket 1", 10, 11),
        Ticket("Ticket 2", 9, 10),
        Ticket("Ticket 3", 12, 14),
        Ticket("Ticket 4", 19, 20),
        Ticket("Ticket 5", 16, 18)
    )

    // Декоратор отступов
    private val offsetsDecor = ColumnOffsetDecor()

    private val columnViewController = ColumnViewController(R.layout.layout_time_line_column)

    private val decorator by lazy {
        Decorator.Builder()
            .overlay(BarsDecor(this))
//            .overlay(columnViewController.viewType() to DutyTimeDecor(this))
            .offset(columnViewController.viewType() to offsetsDecor)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_line_advanced)

        init()
    }

    private fun init() {
        recyclerView.apply {
            addItemDecoration(decorator)
            layoutManager = TimeLineLayoutManager(context)/*LinearLayoutManager(
                this@TimeLineAdvancedActivity,
                RecyclerView.HORIZONTAL, false
            )*/

            // это padding'и для служебных панелей
            val leftPadding = context.resources.getDimensionPixelSize(R.dimen.left_bar_width)
            val topPadding = context.resources.getDimensionPixelSize(R.dimen.top_bar_height)

            adapter = smartAdapter
            setPadding(leftPadding, topPadding, 0, 0)
        }

        SmartList.create().apply {
            addItems(tickets.map { TicketItem(it, columnViewController) })
        }.also(smartAdapter::updateModel)

    }
}