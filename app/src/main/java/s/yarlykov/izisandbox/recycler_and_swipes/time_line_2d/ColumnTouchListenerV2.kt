package s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d

import android.content.Context
import android.graphics.RectF
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import s.yarlykov.izisandbox.extensions.forceSiblingsToDo
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.model.Ticket
import s.yarlykov.izisandbox.utils.logIt
import kotlin.math.ceil

class ColumnTouchListenerV2(
    private val context: Context,
    private val ticket: Ticket
) : View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener {

    enum class State {
        Translating,
        Scaling,
        None
    }

    private var state: State = State.None

    private lateinit var view: View

    private val initDuration = ticket.end - ticket.start

    /**
     * Для обработки событий onTouch
     */
    private val blueRect = RectF()

    private var activePointerId = 0
    private val pointers = mutableMapOf<Int, Float>()

    private val scaleDetector = ScaleGestureDetector(context, this)

    /**
     * Обрабатываем zoom двумя пальцами
     */
    private var lastSpanY: Float = 0f

    /**
     * Захват события и блокировка скрола у родителя:
     * - одиночный/двойной тач внутри blueRect
     * - двойной тач за пределами blueRect
     */
    private var lastEventY = 0f
    override fun onTouch(view: View, event: MotionEvent): Boolean {

        this.view = view

        scaleDetector.onTouchEvent(event)
        handleTouchEvent(view, event)

//        if (state != State.Scaling) {
//            handleTouchEvent(view, event)
//        }

        return true
    }

    private fun handleTouchEvent(view: View, event: MotionEvent): Boolean {

        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                logIt("Listener:ACTION_DOWN")
                lastEventY = event.y

                if (insideBlueRegion(event, view)) {
                    logIt("Listener:ACTION_DOWN, insideBlueRegion")
                    state = State.Translating
                    view.apply {
                        parent.requestDisallowInterceptTouchEvent(true)
                        forceSiblingsToDo { isSelected = false }
                        isSelected = true
                        (parent as ViewGroup).invalidate()
                    }
                    true
                } else {
                    false
                }
            }
            MotionEvent.ACTION_MOVE -> {
                logIt("Listener:ACTION_MOVE")
                if (event.pointerCount == 1 && view.isSelected/* && state == State.Translating*//*&& !scaleDetector.isInProgress*/) {
                    logIt("Listener:ACTION_MOVE, Translating")
                    translateBlueRect(event.y - lastEventY, view)
                    lastEventY = event.y
                    true
                } else false

            }
            MotionEvent.ACTION_UP -> {
                logIt("Listener:ACTION_UP")
                view.parent.requestDisallowInterceptTouchEvent(false)
                state = State.None
                true
            }
            MotionEvent.ACTION_CANCEL -> {
                logIt("Listener:ACTION_CANCEL")
                true
            }
            else -> false
        }
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        logIt("Scale: Begin")
        view.parent.requestDisallowInterceptTouchEvent(true)
        state = State.Scaling
        return true
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        logIt("Scale: in process")


        val duration = detector.scaleFactor * (ticket.end - ticket.start)

        ticket.end = ticket.start + duration.toInt()
        (view.parent as ViewGroup).invalidate()
        state = State.Scaling
        return true

    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        view.parent.requestDisallowInterceptTouchEvent(false)
        logIt("Scale: End")
        state = State.None
    }

    /**
     * Попробовать захватить событие одиночного тача. Для этого нужно определить положение
     * blueRect и проверить, что координаты event'а попадают в него. Значения event.x/y определены
     * в координатах view, поэтому blueRect тоже создается в координатах view (в декораторах
     * наоборот используются координаты относительно родителя потому что там отрисовка на канве
     * родителя)
     */
    private fun insideBlueRegion(event: MotionEvent, view: View): Boolean {
        val dayRange = ticket.dayRange

        // ppm - pixels per minute
        val ppm = (view.height).toFloat() / (dayRange.last - dayRange.first)

        /**
         * Координаты в event (x/y) относительно родителя. Поэтому blueRect тоже должен
         * иметь координаты относительно родителя.
         */
        blueRect.set(
            view.left.toFloat(),
            view.top + (ticket.start - dayRange.first) * ppm,
            view.right.toFloat(),
            view.top + (ticket.end - dayRange.first) * ppm
        )

        val (eventX, eventY) = event.x to event.y

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
        state = State.Translating
        (view.parent as ViewGroup).invalidate()
    }
}