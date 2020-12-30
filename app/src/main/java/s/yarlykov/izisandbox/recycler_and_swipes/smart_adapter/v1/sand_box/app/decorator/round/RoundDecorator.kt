package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v1.sand_box.app.decorator.round

import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v1.sand_box.app.decorator.Decorator

/**
 * Его задача установить кастомный ViewOutlineProvider, который будет возвращать форму
 * для обрезки View.
 */
class RoundDecorator(private val cornerRadius: Float) : Decorator.ViewHolderDecorator {

    override fun draw(
        canvas: Canvas,
        view: View,
        recyclerView: RecyclerView,
        state: RecyclerView.State
    ) {
        if (cornerRadius != 0f) {
            val roundMode = RoundMode.BOTTOM
            val outlineProvider = view.outlineProvider

            if (outlineProvider is RoundOutlineProvider) {
                outlineProvider.roundMode = roundMode
                view.invalidateOutline()
            } else {
                view.outlineProvider = RoundOutlineProvider(cornerRadius, roundMode)
                view.clipToOutline = true
            }
        }
    }
}