package s.yarlykov.izisandbox.recycler_and_swipes.debug_events

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_recycler_view_events.*
import s.yarlykov.izisandbox.R

/**
 * Эта активити была создана для разработки TimeLine 2D, чтобы увидеть весь
 * цикл прохождения событий между Parent-Child в RecyclerView.
 */
class RecyclerViewEventsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view_events)
        init()
    }

    private fun init() {
        stubRecycler.apply {
            adapter = AdapterItemsStub(this@RecyclerViewEventsActivity)
            layoutManager = LinearLayoutManager(this@RecyclerViewEventsActivity)
        }
    }
}