package s.yarlykov.izisandbox.recycler.swipe_2

import android.view.MotionEvent
import android.view.View

class DragGestureDetector : View.OnTouchListener {

    private var dX = 0f
    private var rawTouchDownX = 0f
    private var firstTouchDown = false

    override fun onTouch(view: View, event: MotionEvent): Boolean {

        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                view.parent.requestDisallowInterceptTouchEvent(true)
                firstTouchDown = true

                rawTouchDownX = event.rawX
                dX = view.x - event.rawX
                true
            }
            MotionEvent.ACTION_MOVE -> {

                true
            }
            MotionEvent.ACTION_UP -> {
                view.parent.requestDisallowInterceptTouchEvent(false)


                firstTouchDown = false
                true

            }
            else -> {
                false
            }

        }

    }
}