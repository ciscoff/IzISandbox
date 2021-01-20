package s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.model

/**
 * @param workTimeStart - начало рабочего дня в минутах, выровненное на границу часа (кратно 60)
 * @param workTimeStart - конец рабочего дня в минутах, выровненное на границу часа (кратно 60)
 * @param busySlots - масссив диапазонов нерабочего (или недоступного) времени (в минутах)
 * @param start - начало выбранного диапазона в минутах
 * @param end - конец выбранного диапазона в минутах
 */
data class Ticket(
    val title: String,
    var start: Int,
    var end: Int,
    private val workTimeStart: Int,
    private val workTimeEnd: Int,
    val busySlots: List<Pair<Int, Int>>
) {
    companion object {
        private const val HOUR_MINUTES = 60
    }

    /**
     * Возвращаем диапазон рабочего времени округляя крайние значения до 1 часа (60 минут).
     * Нижнее значение в меньшую сторону. Верхнее в большую.
     */
    val dayRange: IntRange by lazy {

        // 125 => 120
        val wts = workTimeStart - workTimeStart % HOUR_MINUTES

        // 128 => 180
        val reminder = workTimeEnd % HOUR_MINUTES
        val wte = (workTimeEnd - reminder) + (if(reminder != 0) 1 else 0) * HOUR_MINUTES

        (wts..wte)
    }
}