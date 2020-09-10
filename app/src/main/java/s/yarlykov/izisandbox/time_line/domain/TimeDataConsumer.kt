package s.yarlykov.izisandbox.time_line.domain

interface TimeDataConsumer {
    fun initialize(
        _timeData: TimeData,
        _schedule: List<DateRange>,
        _severityMode: SeverityMode
    )

    fun setOnTimeChangeListener(listener : (Int, Int) -> Unit)

}