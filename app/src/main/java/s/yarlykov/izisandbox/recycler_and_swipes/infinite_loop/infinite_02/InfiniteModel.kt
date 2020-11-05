package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_02

import org.threeten.bp.LocalDate
import s.yarlykov.izisandbox.extensions.ZDate

class InfiniteModel : OverScrollListener {

    companion object {
        const val MODEL_SIZE = Int.MAX_VALUE
        const val VIEW_PORT_CAPACITY = 4
    }

    private var direction = 1
    val size = MODEL_SIZE

    operator fun get(i: Int): LocalDate {

        val now = ZDate.now().toLocalDate()

        return if (direction > 0) {
            now.plusDays(i.toLong())
        } else {
            now.minusDays(i.toLong())
        }
    }

    override fun onTopOverScroll() {
        direction = -1
    }

    override fun onBottomOverScroll() {
        direction = 1
    }
}