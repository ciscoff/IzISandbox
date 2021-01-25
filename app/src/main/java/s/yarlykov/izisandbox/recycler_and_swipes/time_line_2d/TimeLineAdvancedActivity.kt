package s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d

import android.animation.ValueAnimator
import android.graphics.Rect
import android.os.Bundle
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomappbar.BottomAppBar
import kotlinx.android.synthetic.main.activity_time_line_advanced.*
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.Decorator
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.adapter.SmartAdapterV2
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.model.SmartList
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.decors.HolderAnyOverlayDecor
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.decors.HolderOffsetDecor
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.decors.HolderViewTypeOverlayDecor
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.decors.RvOverlayDecor
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.model.TicketItem
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.model.Tickets
import kotlin.properties.Delegates

class TimeLineAdvancedActivity : AppCompatActivity() {

    private var isBarVisible: Boolean by Delegates.notNull()

    private val smartAdapter = SmartAdapterV2()

    // Декоратор отступов
    private val offsetsDecor = HolderOffsetDecor()

    private val columnViewController = ColumnViewController(R.layout.layout_time_line_column)

    private val decorator by lazy {
        Decorator.Builder()
            .overlay(RvOverlayDecor(this))
            .overlay(columnViewController.viewType() to HolderViewTypeOverlayDecor(this))
            .overlay(HolderAnyOverlayDecor(this))
            .offset(columnViewController.viewType() to offsetsDecor)
            .build()
    }

    private var scaleFrom: Float = 0f
    private var scaleTo: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_line_advanced)
        init()
    }

    private fun init() {
        // Значения для zoom'а
        val typedValue = TypedValue()
        resources.getValue(R.dimen.scale_min, typedValue, true)
        scaleFrom = typedValue.float
        resources.getValue(R.dimen.scale_max, typedValue, true)
        scaleTo = typedValue.float

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
            addItems(Tickets.model.map { TicketItem(it, columnViewController) })
        }.also(smartAdapter::updateModel)

        bottomBar.apply {
            initState = Hidden
            initElevation = 0f
        }

        numberSlider.apply {
            valueFrom = scaleFrom
            valueTo = scaleTo
//            stepSize = 1f
            value = scaleFrom
            addOnChangeListener { _, value, _ ->
                (recyclerView.layoutManager as ZoomConsumer).onZoomChanged(value)
                recyclerView.requestLayout()
            }
        }
    }

    private val onTouchListener = object : View.OnTouchListener {

        var isEventCaught = false

        override fun onTouch(v: View, event: MotionEvent): Boolean {

            val zoomArea = Rect(0, 0, v.paddingLeft, v.paddingTop)

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    isEventCaught = zoomArea.contains(event.x.toInt(), event.y.toInt())
                    return isEventCaught
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
                        return true
                    }
                }
            }
            return false
        }
    }

    /**
     * Костылёк ))
     *
     * Анимация зума
     */
    fun animateZoom() {
        if (numberSlider.value == scaleTo) return

        ValueAnimator.ofFloat(numberSlider.value, scaleTo).apply {
            duration = 500L

            addUpdateListener { animator ->
                numberSlider.value = animator.animatedValue as Float
                (recyclerView.layoutManager as ZoomConsumer).onZoomChanged(numberSlider.value)
                recyclerView.requestLayout()
            }
        }.start()
    }

    private var BottomAppBar.initState: BottomBarState
        set(value) {
            when (value) {
                Hidden -> {
                    isBarVisible = false
                    postDelayed({ performHide() }, 100)
                }
                Visible -> {
                    isBarVisible = true
                    bottomBar.visibility = View.VISIBLE
                }
            }
        }
        get() = Hidden

    private var BottomAppBar.initElevation: Float
        set(value) {
            elevation = value
        }
        get() = 0f
}