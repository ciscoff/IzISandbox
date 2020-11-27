package s.yarlykov.izisandbox.recycler_and_swipes.swipe_4.animation

import android.animation.Animator
import android.animation.ObjectAnimator
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import s.yarlykov.izisandbox.utils.logIt
import s.yarlykov.izisandbox.extensions.animateBack
import s.yarlykov.izisandbox.extensions.findRecyclerViewParent
import s.yarlykov.izisandbox.extensions.viewHierarchyActivationLoop
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_animation.AnimatorListenerTemplate
import kotlin.math.abs

/**
 * Использован материал
 * https://github.com/rambler-digital-solutions/swipe-layout-android
 */


/**
 * Класс обрабатывает свайпы карточек услуг.
 *
 * Отдельная фича: когда карточка установлена в положение Waiting и в родительском
 * RecyclerView происходит вертикальный скролл, то нужно анимировать карточку
 * в исходное положение. Для этого используется RecyclerView.OnScrollListener в
 * родительском фрагменте (см. recyclerScrollListener)
 *
 */
class ItemDragHandlerV4 : View.OnTouchListener {

    enum class Position {
        Start,
        Waiting
    }

    private val TAG_DRAG = "TAG_DRAG"

    private var dX = 0f
    private var rawTouchDownX = 0f
    private var rawTouchDownY = 0f
    private val duration = 80L

    private var viewGlobalX = 0

    private var currentPosition =
        Position.Start

    private var touchSlop = 0

    private fun animator(
        view: View,
        shift: Float,
        animationDuration: Long,
        listener: Animator.AnimatorListener
    ) = ObjectAnimator.ofFloat(view, "translationX", shift).apply {
        interpolator = LinearInterpolator()
        duration = animationDuration
        addListener(listener)
    }

    private val animateToWaitPosition = AnimatorListenerTemplate(
        onStart = {
        },
        onEnd = {
            currentPosition =
                Position.Waiting
        })

    private val animateToStartPosition = AnimatorListenerTemplate(
        onStart = {
        },
        onEnd = {
            currentPosition =
                Position.Start
        })

    /**
     * При первом касании проверяем область тача и фиксируем координаты
     */
    private fun onTouchBegin(view: View, event: MotionEvent): Boolean {

        touchSlop = ViewConfiguration.get(view.context).scaledTouchSlop

        val rect = Rect()
        view.getGlobalVisibleRect(rect)
        viewGlobalX = rect.left

        // Реагируем на тачи в верхней области View
        return if (event.rawY - rect.top <= rect.height() / 3) {
            rawTouchDownX = event.rawX
            rawTouchDownY = event.rawY

            dX = viewGlobalX - event.rawX
            true
        } else {
            false
        }
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {

        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                onTouchBegin(view, event)
            }
            MotionEvent.ACTION_MOVE -> {

                val shiftX = abs(event.rawX - rawTouchDownX)
                val shiftY = abs(event.rawY - rawTouchDownY)

                if (shiftX >= touchSlop || shiftY >= touchSlop) {

                    if (shiftY == 0f || shiftX / shiftY > 1f) {

                        view.parent.requestDisallowInterceptTouchEvent(true)

                        when (currentPosition) {
                            Position.Start -> {
                                if (event.rawX < rawTouchDownX && abs(view.x) < view.width / 2) {
                                    view.animate()
                                        .x(event.rawX + dX)
                                        .setDuration(0)
                                        .start()
                                }
                            }
                            Position.Waiting -> {
                                if (event.rawX > rawTouchDownX && view.x <= 0) {
                                    logIt("offset = ${event.rawX + dX - view.width / 2}")
                                    view.animate()
                                        .x(event.rawX + dX - view.width / 2)
                                        .setDuration(0)
                                        .start()
                                }
                            }
                        }
                    }
                }
                true
            }
            MotionEvent.ACTION_UP -> {
                view.parent.requestDisallowInterceptTouchEvent(false)

                when (currentPosition) {
                    Position.Start -> {
                        // Если двигались из стартовой позиции и прошли малый путь, то назад
                        if (abs(view.x) < view.width / 5) {
                            animator(view, 0f, duration, animateToStartPosition).start()
                        }
                        // Если прошли порог, то анимируемся для показа корзинки
                        else {

                            // Только один элемент списка может быть с окрытой корзинкой
                            animatePrevItemBack(view, view.id)

                            animator(
                                view,
                                -(view.width / 2).toFloat(),
                                duration,
                                (view as ViewGroup).blockingAnimatorListener()
                            ).start()
                        }
                    }
                    Position.Waiting -> {
                        if (abs(view.x) < view.width / 2 && (event.rawX > rawTouchDownX)) {
                            animator(
                                view, 0f, duration, (view as ViewGroup).unBlockingAnimatorListener()
                            ).start()
                        }
                    }
                }

                rawTouchDownX = 0f
                true
            }
            // Unknown MotionEvent
            else -> {
                false
            }
        }
    }

    /**
     * При показе корзинки в новом элементе списка закрыть карзинку в предыдущем элементе,
     * если такой элемент существует. Одновременно в списке может быть только один элемент
     * с открытой корзинкой.
     *
     * Алгоритм такой: view - это upperLayer в элементе списка, в котором открывается корзинка.
     * Находим родительский RecyclerView, затем через его LayoutManager делаем проход по всех
     * элементам и выполняем анимацию в них.
     *
     */
    private fun animatePrevItemBack(view: View, itemId : Int) {
        view.findRecyclerViewParent()?.let { rv ->
            rv.layoutManager?.animateBack(view.parent as View, itemId)
        }
    }

    /**
     * По окончании анимации заблокировать все View и иерархии
     */
    private fun ViewGroup.blockingAnimatorListener(exclude: List<Int> = emptyList()) =
        AnimatorListenerTemplate(
            onStart = {},
            onEnd = {
                viewHierarchyActivationLoop(false, exclude)
                // Не забываем обновлять состояние
                currentPosition = Position.Waiting
            }
        )

    /**
     * По окончании анимации разблокировать все View и иерархии
     */
    private fun ViewGroup.unBlockingAnimatorListener(exclude: List<Int> = emptyList()) =
        AnimatorListenerTemplate(
            onStart = {},
            onEnd = {
                viewHierarchyActivationLoop(true, exclude)
                // Не забываем обновлять состояние
                currentPosition = Position.Start
            }
        )
}