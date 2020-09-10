package s.yarlykov.izisandbox.time_line.vZ

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.layout_time_line_compound.view.*
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.hhMm
import s.yarlykov.izisandbox.extensions.hhMmFormatted
import s.yarlykov.izisandbox.extensions.minutes
import s.yarlykov.izisandbox.time_line.domain.*

class TimeLine @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr),
    ViewModelAccessorPro by ViewModelInjectorPro(
        context
    ),
    AttributesAccessor by AttributesInjector(
        context,
        attrs
    ) {

    private lateinit var mockTimeData: TimeData


    init {
        View.inflate(context, R.layout.layout_time_line_compound, this)
        orientation = VERTICAL
        subscribe()
    }

    private fun timeIntervalListener(_low: Int, _high: Int) {
        val low = _low + mockTimeData.startHour.minutes
        val high = _high + mockTimeData.startHour.minutes

        val tmp = "${low.hhMm()} - ${high.hhMm()}"
        tvInterval.text = tmp
        tvDuration.text = (_high - _low).hhMmFormatted()
    }

    /**
     * ********************************************************************************
     * ViewModel Block
     * ********************************************************************************
     *
     * Получение данных из ViewModel
     */
    private fun subscribe() {
        viewModel.timeLineData.observe(activity) {
            val (timeData, schedule) = it

            mockTimeData = timeData

            val titleId =
            when(controlMode) {
                SeverityMode.Client -> R.string.title_client
                SeverityMode.Master -> R.string.title_master
            }

            tvTitle.text = context.getText(titleId)

            (timeSurface as TimeDataConsumer).apply {
                initialize(timeData, schedule, controlMode)
                setOnTimeChangeListener(::timeIntervalListener)
            }
        }
    }

}