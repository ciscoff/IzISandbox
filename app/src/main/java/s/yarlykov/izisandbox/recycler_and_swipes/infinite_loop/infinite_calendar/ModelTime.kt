package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_calendar

import org.threeten.bp.LocalDateTime
import java.util.*

class ModelTime(units: List<Int>, type: Type, from: LocalDateTime) : ModelBase(from),
    List<Int> by units {

    enum class Type {
        Hour,
        Minute
    }

    init {
        val now = from

        val unit = when (type) {
            Type.Hour -> now.hour
            Type.Minute -> now.minute
        }

        /**
         * Нужно сделать rotate исходной коллекции чтобы текущий час/минута оказался
         * в такой позиции адаптера, чтобы отображаться в центральном элементе RecyclerView.
         * То есть (center - i) это расстояние от центрального элемента (со знаком) и на это
         * значение выполняем rotate.
         */
        if (unit > 0) {
            val i = units.indexOf(unit)
            val center = VIEW_PORT_CAPACITY / 2
            Collections.rotate(units, center - i)
        }

        // TODO (Нужно убрать) 'if (unit > 0)' и просто оставить код. В Мастере из-за этого
        // TODO возникала ошибка, если требовалось показать время 0 минут (например 11:00)
    }
}