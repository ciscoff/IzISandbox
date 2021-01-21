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
        PreScale,
        Scaling,
        PreTranslate,
        Translating,
        None
    }

    private var state: State = State.None

    private lateinit var columnView: View

    private val initDuration = ticket.end - ticket.start

    /**
     * Для обработки событий onTouch
     */
    private var activePointerId = 0
    private val blueRect = RectF()

    private val pointers = mutableMapOf<Int, Float>()

    private val scaleDetector = ScaleGestureDetector(context, this)

    /**
     * Обрабатываем zoom двумя пальцами
     */
    private var lastSpanY: Float = 0f

    private var routeToScale = false

    /**
     * Захват события и блокировка скрола у родителя:
     * - одиночный/двойной тач внутри blueRect
     * - двойной тач за пределами blueRect
     */
    private var lastEventY = 0f
    override fun onTouch(view: View, event: MotionEvent): Boolean {

        if (!::columnView.isInitialized) {
            columnView = view
        }

        return when(evaluateState(view, event)) {
            // Тапнули за пределами blueRect
            State.PreScale -> {
                scaleDetector.onTouchEvent(event)
                true
            }
            // Масштабируем blueRect
            State.Scaling -> {
                scaleDetector.onTouchEvent(event)
                true
            }
            // Тапнули внутри blueRect
            State.PreTranslate -> {
                true
            }
            // Тянем blueRect
            State.Translating -> {
                true
            }

            State.None -> {
                handleTouchEvent(view, event)
            }
        }
//
//        val result = scaleDetector.onTouchEvent(event)
//
//        if (state != State.Scaling) {
//            return handleTouchEvent(view, event)
//        }
//
//        return result
    }

    private var isDragCaught = false

    private fun evaluateState(view: View, event: MotionEvent) : State{

        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {

                lastEventY = event.y

                if (insideBlueRegion(event, view)) {
                    view.apply {
                        parent.requestDisallowInterceptTouchEvent(true)
                        forceSiblingsToDo { isSelected = false }
                        isSelected = true
                        state = State.PreTranslate
                        (parent as ViewGroup).invalidate()

                    }
                } else {
                    state = State.PreScale
                }
                true
            }

        }

        return State.None

    }


//    override fun onTouch(view: View, event: MotionEvent): Boolean {
//
//        if(!::columnView.isInitialized) {
//            columnView = view
//        }
//
//        when (event.actionMasked) {
//            MotionEvent.ACTION_DOWN -> {
//                pointers.clear()
//                activePointerId = event.getPointerId(0)
//                pointers[activePointerId] = event.getY(0)
//                lastEventY = event.y
//
//                if (insideBlueRegion(event, view)) {
//                    view.apply {
//                        parent.requestDisallowInterceptTouchEvent(true)
//                        forceSiblingsToDo { isSelected = false }
//                        isSelected = true
//                        (parent as ViewGroup).invalidate()
//                        routeToScale = true
//                    }
//                } else {
//                    return false
//                }
//            }
//        }
//
//        return if(routeToScale) {
//            scaleDetector.onTouchEvent(event)
//            handleTouchEvent(view, event)
//        } else false
//
////        scaleDetector.onTouchEvent(event)
////        return handleTouchEvent(view, event)
//    }

    private fun handleTouchEvent(view: View, event: MotionEvent): Boolean {

        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {

                lastEventY = event.y

                if (insideBlueRegion(event, view)) {
                    view.apply {
                        parent.requestDisallowInterceptTouchEvent(true)
                        forceSiblingsToDo { isSelected = false }
                        isSelected = true
                        state = State.PreTranslate
                        (parent as ViewGroup).invalidate()

                    }
                } else {
                    state = State.PreScale
                }
                true
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount == 1 && view.isSelected /*&& !scaleDetector.isInProgress*/) {
                    translateBlueRect(event.y - lastEventY, view)
                    lastEventY = event.y
                }
                true
            }
            MotionEvent.ACTION_UP -> {
                view.parent.requestDisallowInterceptTouchEvent(false)
//                routeToScale = false
                state = State.None
                true
            }
            else -> false
        }
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        columnView.parent.requestDisallowInterceptTouchEvent(true)
        return true
    }

    private var scaleFactor = 1f

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        logIt("scaleFactor=$scaleFactor, detector.scaleFactor=${detector.scaleFactor}")

        val duration = detector.scaleFactor * (ticket.end - ticket.start)

        ticket.end = ticket.start + duration.toInt()
        (columnView.parent as ViewGroup).invalidate()

        return true

    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        state = State.Scaling
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
        state = State.Translating
        (view.parent as ViewGroup).invalidate()
    }

}