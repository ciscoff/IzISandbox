package s.yarlykov.izisandbox.time_line.v3

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import s.yarlykov.izisandbox.utils.zipLiveData
import s.yarlykov.izisandbox.time_line.domain.DateRange
import s.yarlykov.izisandbox.time_line.domain.TimeData
import s.yarlykov.izisandbox.time_line.domain.TimeSlotType

class TimeLineViewModelV3 : ViewModel() {

    private val mockTimeData = TimeData(
        startHour = 8,
        endHour = 22,
        itemDuration = 60,
        frameStartPosition = DateRange(11, 13),
        timeSlotType = TimeSlotType.SlotFixStart,
        timeSlotValue = 60
    )

    private val mockSchedule = listOf(
        DateRange(8, 9),
        DateRange(11, 14),
        DateRange(15, 17),
        DateRange(19, 20),
        DateRange(21, 22))

    private val timeData = MutableLiveData<TimeData>().apply {
        value = mockTimeData
    }

    private val schedule = MutableLiveData<List<DateRange>>().apply {
        value = mockSchedule
    }

    /**
     * Начальные данные для отрисовки компонента TimeSurface
     */
    val timeLineData = zipLiveData(timeData, schedule)

}