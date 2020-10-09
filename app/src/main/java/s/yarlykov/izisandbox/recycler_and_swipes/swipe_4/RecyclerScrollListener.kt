package s.yarlykov.izisandbox.recycler_and_swipes.swipe_4

import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.viewHierarchyActivationLoop
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_animation.Animators
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_4.animation.ItemDragHandlerV4

/**
 * Задача: среагировать на верт прокрутку тем, чтобы вернуть карточку с открытой
 * красной корзинкой в исходное положение используя анимацию. После её окончания
 * у карточки меняется OnTouchListener потому что прежний продолжает считать что карточка
 * в положении Waiting и не реагирует на свайпы влево.
 */

object RecyclerScrollListener : RecyclerView.OnScrollListener() {

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)

        val lm: LinearLayoutManager = recyclerView.layoutManager as LinearLayoutManager

        for (i in 0 until lm.childCount) {

            lm.getChildAt(i)?.let { child ->
                val upperLayer = child.findViewById<MaterialCardView>(R.id.upper_layer_4)
                if (upperLayer.x != 0f) {
                    Animators.translateX(upperLayer, 0f, after = {
                        // Не забываем активировать все дочерние View в элементе
                        (upperLayer.parent as ViewGroup).viewHierarchyActivationLoop(true)

                        upperLayer.setOnTouchListener(ItemDragHandlerV4())
                    })
                }
            }
        }

    }
}