package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v1.sand_box.app.decorator

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class MainDecorator(private val controller: DecorController) : RecyclerView.ItemDecoration() {

    /**
     * Рисование уже после вызова onDraw() на view элемента списка
     */
    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(canvas, parent, state)
        controller.drawUnderlay(canvas, parent, state)
    }

    /**
     * Рисование ещё до вызова onDraw() на view элемента списка
     */
    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(canvas, parent, state)

        controller.drawUnderlay(canvas, parent, state)
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        controller.getItemOffsets(outRect, view, parent, state)
    }
}