package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_calendar

import org.threeten.bp.LocalDateTime


abstract class ModelBase(val from: LocalDateTime) {

    companion object {
        const val MODEL_SIZE = Int.MAX_VALUE
        const val VIEW_PORT_CAPACITY = 5
    }
}