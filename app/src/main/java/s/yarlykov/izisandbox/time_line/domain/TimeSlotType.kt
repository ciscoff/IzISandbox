package s.yarlykov.izisandbox.time_line.domain

enum class TimeSlotType(val code: Int) {
    NoSlotable(0),
    SlotFixStart(1),
    SlotFreeStart(2);

    companion object {
        const val minDuration = 5
        fun fromCode(value: Int?) = values().firstOrNull { it.code == value } ?: NoSlotable
    }
}