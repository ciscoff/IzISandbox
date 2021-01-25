package s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.arch.v1

import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import s.yarlykov.izisandbox.extensions.forceSiblingsToDo
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.model.Ticket
import kotlin.math.ceil

/**
 * version : V1
 */

class ColumnTouchListenerV1(private val ticket: Ticket) : View.OnTouchListener {

    private var lastEventY = 0f
    private val blueRect = RectF()

    /**
     * Захват события и блокировка скрола у родителя:
     * - одиночный/двойной тач внутри blueRect
     * - двойной тач за пределами blueRect
     */
    override fun onTouch(view: View, event: MotionEvent): Boolean {

        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (catchTouchEvent(event, view)) {
                    lastEventY = event.y
                    view.apply {
                        parent.requestDisallowInterceptTouchEvent(true)
                        forceSiblingsToDo { isSelected = false }
                        isSelected = true
                        (parent as ViewGroup).invalidate()
                    }
                    true
                } else false
            }
            MotionEvent.ACTION_MOVE -> {
                translateBlueRect(event.y - lastEventY, view)
                lastEventY = event.y
                true
            }
            MotionEvent.ACTION_UP -> {
                view.parent.requestDisallowInterceptTouchEvent(false)
                true
            }
            else -> false
        }
    }

    /**
     * Попробовать захватить событие одиночного тача. Для этого нужно определить положение
     * blueRect и проверить, что координаты event'а попадают в него. Значения event.x/y определены
     * в координатах view, поэтому blueRect тоже создается в координатах view (в декораторах
     * наоборот используются координаты относительно родителя потому что там отрисовка на канве
     * родителя)
     */
    private fun catchTouchEvent(event: MotionEvent, view: View): Boolean {
        val dayRange = ticket.dayRange

        // ppm - pixels per minute
        val ppm = (view.height).toFloat() / (dayRange.last - dayRange.first)

        blueRect.set(
            0f,
            (ticket.start - dayRange.first) * ppm,
            view.width.toFloat(),
            (ticket.end - dayRange.first) * ppm
        )

        return blueRect.contains(event.x, event.y)
    }

    /**
     * Переместить blueRect вертикально. Проверить на границы view.
     */
    private fun translateBlueRect(offset: Float, view: View) {
        val dayRange = ticket.dayRange

        // ppm - pixels per minute
        val ppm = (view.height).toFloat() / (dayRange.last - dayRange.first)

        blueRect.set(
            0f,
            (ticket.start - dayRange.first) * ppm,
            view.width.toFloat(),
            (ticket.end - dayRange.first) * ppm
        )

        // Обрабатываем крайние условия
        val dY = if (offset > 0) {
            if (blueRect.bottom + offset < view.height) offset else view.height - blueRect.bottom
        } else {
            if (blueRect.top + offset > 0) offset else -blueRect.top
        }

        val minutes = ceil(dY / ppm).toInt()

        ticket.start += minutes
        ticket.end += minutes
        (view.parent as ViewGroup).invalidate()
    }
}