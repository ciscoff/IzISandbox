package s.yarlykov.izisandbox.recycler_and_swipes.time_line

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.extensions.px
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.Decorator

class ColumnOffsetDecor : Decorator.OffsetDecorator {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        recyclerView: RecyclerView,
        state: RecyclerView.State
    ) {
        val vh = recyclerView.getChildViewHolder(view)

        outRect.set(0, 56.px, 12.px, 0)

        // У последнего элемента нет правого offset
        if (recyclerView.adapter?.itemCount == (vh.adapterPosition + 1)) {
            outRect.right = 0.px
        }
    }
}