package s.yarlykov.izisandbox.time_line.v2

import io.reactivex.Observable
import s.yarlykov.izisandbox.time_line.v2.domain.DateRange
import s.yarlykov.izisandbox.time_line.v2.domain.TimeData

interface TimeLineView {
    fun onTimeData(timeDataObs: Observable<TimeData>)
    fun onSchedulerData(schedulerDataObs: Observable<List<DateRange>>)
}