package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_calendar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.dsl.extenstions.dp_f
import s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_calendar.ModelBase.Companion.VIEW_PORT_CAPACITY
import kotlin.math.abs

class ScrollingCalendar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        const val METRICS_STROKE_WIDTH_DP = 0.6f
    }

    private val bgColor = ContextCompat.getColor(context, android.R.color.white)
    private val paintText = Paint().apply {
        color = ContextCompat.getColor(context, R.color.color_calendar_selection_border)
        strokeWidth = dp_f(METRICS_STROKE_WIDTH_DP)
    }

    private val listHours: RecyclerView
    private val listDates: RecyclerView
    private val listMinutes: RecyclerView

    private val modelHours = ModelTime((0 until 24).toList(), ModelTime.Type.Hour)
    private val modelMinutes = ModelTime((0 until 60).toList(), ModelTime.Type.Minute)
    private val modelDates = ModelDate()

    init {

        // Разрешить рисование для ViewGroup
        setWillNotDraw(false)

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

    /**
     * Рисуем две горизонтальные линии для выделения центрального элемента. Это декор.
     */
    override fun onDraw(canvas: Canvas) {

        val slotHeight = height / VIEW_PORT_CAPACITY
        var centerSlot = VIEW_PORT_CAPACITY / 2

        val startY1 = centerSlot++ * slotHeight
        val startY2 = centerSlot * slotHeight

        canvas.drawColor(bgColor)

        canvas.drawLine(0f, startY1.toFloat(), width.toFloat(), startY1.toFloat(), paintText)
        canvas.drawLine(0f, startY2.toFloat(), width.toFloat(), startY2.toFloat(), paintText)
    }

    /**
     * Вернуть выбранные Дату-Время
     */
    val selectedDateTime: LocalDateTime
        get() {
            val now = LocalDate.now()

            val dayOffset =
                (listDates.layoutManager as BaseLayoutManager).closestToCenter?.tag as Int
            val hour = (listHours.layoutManager as BaseLayoutManager).closestToCenter?.tag as Int
            val minutes =
                (listMinutes.layoutManager as BaseLayoutManager).closestToCenter?.tag as Int

            val date = when {
                dayOffset > 0 -> now.plusDays(dayOffset.toLong())
                dayOffset < 0 -> now.minusDays(abs(dayOffset.toLong()))
                else -> now
            }

            return LocalDateTime.of(date, LocalTime.of(hour, minutes))
        }
}