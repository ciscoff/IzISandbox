package s.yarlykov.izisandbox.recycler_and_swipes.grid

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R

class CellDecorator(val context: Context, val spanCount: Int) : RecyclerView.ItemDecoration() {

    private var spacer = context.resources.getDimension(R.dimen.cell_spacer).toInt()

    override fun getItemOffsets(
        rect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view).let {
            if (it == RecyclerView.NO_POSITION) return else it
        }

        val halfSpacer = spacer / 2

        when {
            // Первый столбец
            (position % spanCount == 0) -> {
                rect.left = spacer
                rect.right = halfSpacer
            }
            // Последний столбец
            ((position + 1 % spanCount) == 0) -> {
                rect.left = halfSpacer
                rect.right = spacer

            }
            // Серединка
            else -> {
                rect.left = halfSpacer
                rect.right = halfSpacer
            }
        }
        rect.top = spacer
        rect.bottom = 0
    }
}