package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.sand_box.app.decorator.offset

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.sand_box.app.decorator.Decorator

/**
 * Декоратор устанавливает offsets по сторонам View
 */
class OffsetDecorator(
    private val left: Int = 0,
    private val top: Int = 0,
    private val right: Int = 0,
    private val bottom: Int = 0
) : Decorator.OffsetDecorator {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        recyclerView: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.set(left, top, right, bottom)
    }
}