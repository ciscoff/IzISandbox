package s.yarlykov.izisandbox.recycler_and_swipes.debug_events

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.utils.logIt

class RecyclerViewStub @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {


    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
        val tab = "  "
        val resultSuper: Boolean = super.onInterceptTouchEvent(e)
        logIt("${tab}RecyclerView:Intercept exit with $resultSuper")
        return resultSuper
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val resultSuper: Boolean
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                logIt("RecyclerView:dispatch ACTION_DOWN enter")
                resultSuper = super.dispatchTouchEvent(event)
                logIt("RecyclerView:dispatch ACTION_DOWN exit with $resultSuper")
                logIt("     -------------     ")
                return resultSuper
            }

            // Касание вторым пальцем. Теперь оба пальца на экране.
            MotionEvent.ACTION_POINTER_DOWN -> {
                logIt("RecyclerView:dispatch ACTION_POINTER_DOWN enter")
                resultSuper = super.dispatchTouchEvent(event)
                logIt("RecyclerView:dispatch ACTION_POINTER_DOWN exit with $resultSuper")
                logIt("     -------------     ")
                return resultSuper
            }

            MotionEvent.ACTION_MOVE -> {
                logIt("RecyclerView:dispatch ACTION_MOVE enter")
                resultSuper = super.dispatchTouchEvent(event)
                logIt("RecyclerView:dispatch ACTION_MOVE exit with $resultSuper")
                logIt("     -------------     ")
                return resultSuper
            }

            MotionEvent.ACTION_POINTER_UP -> {
                logIt("RecyclerView:dispatch ACTION_POINTER_UP enter")
                resultSuper = super.dispatchTouchEvent(event)
                logIt("RecyclerView:dispatch ACTION_POINTER_UP exit with $resultSuper")
                logIt("     -------------     ")
                return resultSuper
            }

            MotionEvent.ACTION_UP -> {
                logIt("RecyclerView:dispatch ACTION_UP enter")
                resultSuper = super.dispatchTouchEvent(event)
                logIt("RecyclerView:dispatch ACTION_UP exit with $resultSuper")
                logIt("     -------------     ")
                return resultSuper
            }
            MotionEvent.ACTION_CANCEL -> {
                logIt("RecyclerView:dispatch ACTION_CANCEL enter")
                resultSuper = super.dispatchTouchEvent(event)
                logIt("RecyclerView:dispatch ACTION_CANCEL exit with $resultSuper")
                logIt("     -------------     ")
                return resultSuper
            }
            else -> {
                logIt("RecyclerView:dispatch UNKNOWN [${event.actionMasked}] enter")
                resultSuper = super.dispatchTouchEvent(event)
                logIt("RecyclerView:dispatch UNKNOWN [${event.actionMasked}] exit with $resultSuper")
                logIt("     -------------     ")
                return resultSuper
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val tab = "    "

        val resultSuper: Boolean

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                logIt("${tab}RecyclerView: ACTION_DOWN enter")
                resultSuper = super.onTouchEvent(event)
                logIt("${tab}RecyclerView: ACTION_DOWN exit with $resultSuper")
                return resultSuper
            }

            // Касание вторым пальцем. Теперь оба пальца на экране.
            MotionEvent.ACTION_POINTER_DOWN -> {
                logIt("${tab}RecyclerView: ACTION_POINTER_DOWN enter")
                resultSuper = super.onTouchEvent(event)
                logIt("${tab}RecyclerView: ACTION_POINTER_DOWN exit with $resultSuper")
                return resultSuper
            }

            MotionEvent.ACTION_MOVE -> {
                logIt("${tab}RecyclerView: ACTION_MOVE enter")
                resultSuper = super.onTouchEvent(event)
                logIt("${tab}RecyclerView: ACTION_MOVE exit with $resultSuper")
                return resultSuper
            }

            MotionEvent.ACTION_POINTER_UP -> {
                logIt("${tab}RecyclerView: ACTION_POINTER_UP enter")
                resultSuper = super.onTouchEvent(event)
                logIt("${tab}RecyclerView: ACTION_POINTER_UP exit with $resultSuper")
                return resultSuper
            }

            MotionEvent.ACTION_UP -> {
                logIt("${tab}RecyclerView: ACTION_UP enter")
                resultSuper = super.onTouchEvent(event)
                logIt("${tab}RecyclerView: ACTION_UP exit with $resultSuper")
                return resultSuper
            }
            MotionEvent.ACTION_CANCEL -> {
                logIt("${tab}RecyclerView: ACTION_CANCEL enter")
                resultSuper = super.onTouchEvent(event)
                logIt("${tab}RecyclerView: ACTION_CANCEL exit with $resultSuper")
                return resultSuper
            }
            else -> {
                logIt("${tab}RecyclerView: UNKNOWN [${event.actionMasked}] enter")
                resultSuper = super.onTouchEvent(event)
                logIt("${tab}RecyclerView: UNKNOWN [${event.actionMasked}] exit with $resultSuper")
                return resultSuper
            }
        }
    }
}