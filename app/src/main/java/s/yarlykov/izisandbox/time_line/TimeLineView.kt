package s.yarlykov.izisandbox.time_line

import io.reactivex.Observable
import s.yarlykov.izisandbox.time_line.domain.DateRange
import s.yarlykov.izisandbox.time_line.domain.TimeData

interface TimeLineView {
    fun onTimeData(timeDataObs: Observable<TimeData>)
    fun onSchedulerData(schedulerDataObs: Observable<List<DateRange>>)
}