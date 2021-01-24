package s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d

import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.model.Ticket

/**
 * V4 для работы с ColumnTouchListenerV4
 *
 */
class ScaleGestureListenerV4(
    private val view: View,
    private val ticket: Ticket,
    private val onStateListener: (ColumnTouchListenerV4.State) -> Unit
) : ScaleGestureDetector.OnScaleGestureListener {

    /**
     * Детектор определил начало zoom'a
     */
    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        view.parent.requestDisallowInterceptTouchEvent(true)
        onStateListener(ColumnTouchListenerV4.State.Scale)
        return true
    }

    /**
     * Изменить размер blueRect
     */
    override fun onScale(detector: ScaleGestureDetector): Boolean {
        val duration = detector.scaleFactor * (ticket.end - ticket.start)
        ticket.end = ticket.start + duration.toInt()
        (view.parent as ViewGroup).invalidate()
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
//        view.parent.requestDisallowInterceptTouchEvent(false)
    }
}