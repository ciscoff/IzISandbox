package s.yarlykov.izisandbox.recycler_and_swipes.time_line

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_time_line_advanced.*
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.Decorator
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.adapter.SmartAdapterV2
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.model.SmartList
import s.yarlykov.izisandbox.recycler_and_swipes.time_line.model.Ticket
import s.yarlykov.izisandbox.recycler_and_swipes.time_line.model.TicketItem

class TimeLineAdvancedActivity : AppCompatActivity() {

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
            layoutManager = LinearLayoutManager(
                this@TimeLineAdvancedActivity,
                RecyclerView.HORIZONTAL, false
            )
            adapter = smartAdapter
            setPadding(0, 0, 0, 0)
            addItemDecoration(decorator)
        }

        SmartList.create().apply {
            addItems(tickets.map { TicketItem(it, columnViewController) })
        }.also(smartAdapter::updateModel)
    }
}