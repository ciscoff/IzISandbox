package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_calendar

import org.threeten.bp.LocalDate
import kotlin.math.sign

class ModelDate : ModelBase(), OverScrollListener {

    private var direction = 1
    val size = MODEL_SIZE

    operator fun get(i: Int): LocalDate {
        val now = LocalDate.now()

        return when {
            (direction > 0) -> {
                now.plusDays(i.toLong())
            }
            (direction < 0) -> {
                now.minusDays(i.toLong())
            }
            else -> now
        }
    }

    override fun setOffsetDirection(direction: Int) {
        this.direction = direction.sign
    }
}