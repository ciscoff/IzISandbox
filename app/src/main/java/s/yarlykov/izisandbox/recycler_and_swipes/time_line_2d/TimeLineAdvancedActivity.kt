package s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d

import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomappbar.BottomAppBar
import kotlinx.android.synthetic.main.activity_time_line_advanced.*
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.Decorator
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.adapter.SmartAdapterV2
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.model.SmartList
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.decors.HolderOffsetDecor
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.decors.HolderOverlayDecor
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.decors.RvOverLayDecor
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.model.Ticket
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.model.TicketItem
import kotlin.properties.Delegates

class TimeLineAdvancedActivity : AppCompatActivity() {

    companion object {
        const val DAY_START = 9
        const val DAY_END = 21
    }

    private var isBarVisible: Boolean by Delegates.notNull()

    private val smartAdapter = SmartAdapterV2()

    private val tickets = listOf(
        Ticket("Ticket 1", 10, 11),
        Ticket("Ticket 2", 9, 10),
        Ticket("Ticket 3", 12, 14),
        Ticket("Ticket 4", 19, 20),
        Ticket("Ticket 5", 16, 18)
    )

    // Декоратор отступов
    private val offsetsDecor = HolderOffsetDecor()

    private val columnViewController = ColumnViewController(R.layout.layout_time_line_column)

    private val decorator by lazy {
        Decorator.Builder()
            .overlay(RvOverLayDecor(this))
            .overlay(columnViewController.viewType() to HolderOverlayDecor(this))
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
            layoutManager = TimeLineLayoutManager(context)

            // это padding'и для служебных панелей
            val leftPadding = context.resources.getDimensionPixelSize(R.dimen.left_bar_width)
            val topPadding = context.resources.getDimensionPixelSize(R.dimen.top_bar_height)

            adapter = smartAdapter
            setPadding(leftPadding, topPadding, 0, 0)

            setOnTouchListener(onTouchListener)
        }

        SmartList.create().apply {
            addItems(tickets.map { TicketItem(it, columnViewController) })
        }.also(smartAdapter::updateModel)

        bottomBar.apply {
            initState = hidden
            initElevation = 0f
        }

        numberSlider.apply {
            valueFrom = 1f
            valueTo = 3f
            stepSize = 1f
            value = 1f
            addOnChangeListener { _, value, _ ->
                (recyclerView.layoutManager as ZoomConsumer).onZoomChanged(value.toInt())
                recyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }

    private val onTouchListener = object : View.OnTouchListener {

        var isEventCaught = false

        override fun onTouch(v: View, event: MotionEvent): Boolean {

            val zoomArea = Rect(0, 0, v.paddingLeft, v.paddingTop)

            return when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {

                    if (isBarVisible) {
                        bottomBar.performHide()
                        isBarVisible = !isBarVisible
                    }

                    isEventCaught = zoomArea.contains(event.x.toInt(), event.y.toInt())
                    isEventCaught
                }
                MotionEvent.ACTION_UP -> {

                    if (isEventCaught) {
                        if (isBarVisible) {
                            bottomBar.performHide()
                        } else {
                            bottomBar.visibility = View.VISIBLE
                            bottomBar.performShow()
                        }
                        isBarVisible = !isBarVisible

                        isEventCaught = false
                    }
                    v.performClick()
                }
                else -> {
                    false
                }
            }
        }
    }

    private var BottomAppBar.initState: BottomBarState
        set(value) {
            when (value) {
                hidden -> {
                    isBarVisible = false
                    postDelayed({ performHide() }, 100)
                }
                visible -> {
                    isBarVisible = true
                    bottomBar.visibility = View.VISIBLE
                }
            }
        }
        get() = hidden

    private var BottomAppBar.initElevation: Float
        set(value) {
            elevation = value
        }
        get() = 0f
}