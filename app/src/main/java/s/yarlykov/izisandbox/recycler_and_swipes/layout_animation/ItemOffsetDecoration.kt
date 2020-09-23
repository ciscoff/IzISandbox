package s.yarlykov.izisandbox.recycler_and_swipes.layout_animation

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R

class ItemOffsetDecoration(
    context: Context
) : RecyclerView.ItemDecoration() {

    private val spacing = context.resources.getDimensionPixelOffset(R.dimen.default_spacing_small)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) = outRect.set(spacing, spacing, spacing, spacing)
}