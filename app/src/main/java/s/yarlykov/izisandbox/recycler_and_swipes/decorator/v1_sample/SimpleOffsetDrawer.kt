package s.yarlykov.izisandbox.recycler_and_swipes.decorator.v1_sample

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SimpleOffsetDrawer(
    private val left: Int = 0,
    private val top: Int = 0,
    private val right: Int = 0,
    private val bottom: Int = 0
) : Decorator.OffsetDecor {

    constructor(offset: Int) : this(offset, offset, offset, offset)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        recyclerView: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.set(left, top, right, bottom)
    }
}