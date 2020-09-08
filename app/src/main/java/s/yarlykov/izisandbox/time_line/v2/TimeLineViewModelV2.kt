package s.yarlykov.izisandbox.time_line.v2

import io.reactivex.Observable
import s.yarlykov.izisandbox.time_line.v2.domain.DateRange
import s.yarlykov.izisandbox.time_line.v2.domain.TimeData

class TimeLineViewModelV2(
    private val timeData: TimeData,
    private val schedule: List<DateRange>,
    private val timeView: TimeLineView
) {

    init {
        timeView.apply {
            onTimeData(Observable.fromCallable { timeData })
            onSchedulerData(Observable.fromCallable { schedule })
        }
    }
}