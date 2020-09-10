package s.yarlykov.izisandbox.time_line.domain

data class TimeData(

    // Начало и конец рабочего дня
    val startHour: Int,
    val endHour: Int,

    // Продолжительность услуги
    val itemDuration: Int,

    // Начальная позиция ползунка
    val frameStartPosition: DateRange,

    // Слотовость
    val timeSlotType: TimeSlotType = TimeSlotType.NoSlotable,

    // Величина тайм-слота (у NoSlotable сервисов timeSlotValue == minDuration)
    val timeSlotValue: Int = TimeSlotType.minDuration
) {
    val hoursQty: Int
        get() = endHour - startHour

    override fun toString(): String {
        return "TimeLine startHour=$startHour,  endHour=$endHour, itemDuration=$itemDuration, frameWidgetStartPosition=$frameStartPosition, timeSlotType=$timeSlotType, timeSlotValue=$timeSlotValue"
    }
}