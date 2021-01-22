package s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.extensions.findRecyclerViewParent

class ColumnView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /**
     * У всех элементов списка должны быть уникальные ID чтобы можно было однозначно
     * идентифицировать любой из них и поручать ему выполнить определенные действия.
     * В данной реализации декоратор первого видимого элемента списка рисует шкалу времени.
     */
    init {
        id = generateViewId()
    }

//    override fun onTouchEvent(event: MotionEvent): Boolean {
//
//        when (event.actionMasked) {
//            MotionEvent.ACTION_DOWN -> {
//            }
//            MotionEvent.ACTION_MOVE -> {
//            }
//            MotionEvent.ACTION_POINTER_UP -> {
//            }
//        }
//
//        return false
//    }
}