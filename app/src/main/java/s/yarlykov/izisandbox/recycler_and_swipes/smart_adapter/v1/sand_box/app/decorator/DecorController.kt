package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v1.sand_box.app.decorator

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.extensions.px
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v1.sand_box.app.decorator.Decorator.EACH_VIEW
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v1.sand_box.app.decorator.round.RoundDecorator

class DecorController(
    private val underlays: List<DecorBinder<Decorator.ViewHolderDecorator>>,
    private val offsets: List<DecorBinder<Decorator.OffsetDecorator>>
) {

    /**
     * Сконвертировать список в hashMap. На один К - только один V, где К = viewType,
     * V = элемент исходного списка.
     */
    private val associatedOffsets = offsets.associateBy { it.viewType }

    /**
     * Сконвертировать список в hashMap. На один K может получиться несколько V, где К = viewType,
     * V = элемент списка.
     */
    private val groupedUnderlays = underlays.groupBy { it.viewType }


    private val roundDecorator = RoundDecorator(12.px.toFloat())

    fun drawUnderlay(canvas: Canvas, recyclerView: RecyclerView, state: RecyclerView.State) {

        groupedUnderlays.drawNotAttachedDecors(canvas, recyclerView, state)
        groupedUnderlays.drawAttachedDecors(canvas, recyclerView, state)
    }

    fun drawOverlay(canvas: Canvas, recyclerView: RecyclerView, state: RecyclerView.State) {

    }

    fun getItemOffsets(
        outRect: Rect,
        view: View,
        recyclerView: RecyclerView,
        state: RecyclerView.State
    ) {
//        drawOffset(EACH_VIEW, outRect, view, recyclerView, state)
        recyclerView.findContainingViewHolder(view)?.itemViewType?.let { itemViewType ->
            drawOffset(itemViewType, outRect, view, recyclerView, state)
        }
    }

    private fun Map<Int, List<DecorBinder<Decorator.ViewHolderDecorator>>>.drawAttachedDecors(
        canvas: Canvas,
        recyclerView: RecyclerView,
        state: RecyclerView.State
    ) {

        recyclerView.children.forEach { view ->
            val viewType = recyclerView.getChildViewHolder(view).itemViewType
            this[viewType]?.forEach {
                it.decorator.draw(canvas, view, recyclerView, state)
            }
        }
    }

    private fun Map<Int, List<DecorBinder<Decorator.ViewHolderDecorator>>>.drawNotAttachedDecors(
        canvas: Canvas,
        recyclerView: RecyclerView,
        state: RecyclerView.State
    ) {
        recyclerView.children.forEach { view ->
            this[EACH_VIEW]
                ?.forEach { it.decorator.draw(canvas, view, recyclerView, state) }
        }
    }

    private fun drawOffset(
        viewType: Int,
        outRect: Rect,
        view: View,
        recyclerView: RecyclerView,
        state: RecyclerView.State
    ) {
        associatedOffsets[viewType]
            ?.decorator
            ?.getItemOffsets(outRect, view, recyclerView, state)
    }
}