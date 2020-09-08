package s.yarlykov.izisandbox.time_line

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_time_line.*
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.time_line.v2.TimeLineView
import s.yarlykov.izisandbox.time_line.v2.TimeLineViewModelV2
import s.yarlykov.izisandbox.time_line.v2.domain.DateRange
import s.yarlykov.izisandbox.time_line.v2.domain.TimeData
import s.yarlykov.izisandbox.time_line.v2.domain.TimeSlotType

/**
 * Думаю можно обойтись контейнером FrameLayout и двумя дочерними View:
 * - одна для background (свободное/занятое время двумя цветами, шкала и раочие часы)
 * - вторая для ползунка (просто View и background как рамка)
 */

class TimeLineActivity : AppCompatActivity() {

    private val mockTimeData = TimeData(
        startHour = 10,
        endHour = 21,
        itemDuration = 60,
        frameStartPosition = DateRange(13, 14),
        timeSlotType = TimeSlotType.SlotFreeStart,
        timeSlotValue = 60
    )

    private val mockSchedule = listOf(DateRange(11, 13), DateRange(15, 17), DateRange(20, 21))


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_line)
    }

    override fun onResume() {
        super.onResume()
        initTimeLine()
    }

    private fun initTimeLine() {
        TimeLineViewModelV2(mockTimeData, mockSchedule, tsTimeLine as TimeLineView)
    }
}