package s.yarlykov.izisandbox.telegram.v2

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R

class ListItemDecorator (
    context: Context
) : RecyclerView.ItemDecoration() {

    private val spacing = context.resources.getDimensionPixelOffset(R.dimen.default_spacing_medium)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) = outRect.set(0, 0, 0, spacing)
}