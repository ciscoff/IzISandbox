package s.yarlykov.izisandbox.extensions

import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.recycler_and_swipes.animation.Animators
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_4.animation.ItemDragHandlerV4

/**
 * Показать SnackBar
 */
fun View.showSnackBarNotification(message: String, callback: Snackbar.Callback? = null) {
    Snackbar.make(this, message, Snackbar.LENGTH_LONG).apply {
        setActionTextColor(ContextCompat.getColor(view.context, R.color.colorAccent))

        callback?.let { block ->
            addCallback(block)
        }

    }.show()
}

val View.globLeft: Int
    get() {
        val rect = Rect()
        this.getGlobalVisibleRect(rect)
        return rect.left
    }

/**
 * Рекурсивный обход иерархии View
 */
fun ViewGroup.viewHierarchyActivationLoop(isActive: Boolean, exclude: List<Int> = emptyList()) {
    for (i in 0 until childCount) {
        val child = getChildAt(i)

        if (child.id !in exclude) {
            child.isEnabled = isActive
        }

        if (child is ViewGroup) {
            child.viewHierarchyActivationLoop(isActive, exclude)
        }
    }
}

fun View.findRecyclerViewParent(): RecyclerView? {

    return if (parent !is RecyclerView) {
        (parent as View).findRecyclerViewParent()
    } else {
        parent as RecyclerView
    }
}


/**
 * Ищем среди эелементов RecyclerView всех кроме . Затем ищем в каждом из ни
 * дочерний элемент animatedViewId и анимируем его в исходную Х-позицию.
 */
fun RecyclerView.LayoutManager.animateBack(excludedItem: View, animatedViewId: Int) {

    for (i in 0 until childCount) {

        getChildAt(i)?.let { child ->

            if (child != excludedItem) {
                val upperLayer = child.findViewById<MaterialCardView>(animatedViewId)
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
