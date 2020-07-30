package s.yarlykov.izisandbox.recycler_and_swipes.swipe_4

import android.animation.Animator
import android.animation.ObjectAnimator
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import kotlin.math.abs

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

    private var currentPosition = Position.Start

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
            currentPosition = Position.Waiting
        })

    private val animateToStartPosition = AnimatorListenerTemplate(
        onStart = {
        },
        onEnd = {
            currentPosition = Position.Start
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

                    if (shiftY == 0f || shiftX > shiftY) {

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
                        else /*if (abs(view.x) < view.width)*/ {
                            animator(
                                view,
                                -(view.width / 2).toFloat(),
                                duration,
                                /*animateToWaitPosition*/
                                blockingAnimatorListener(view as ViewGroup)
                            ).start()
                        }
                    }
                    Position.Waiting -> {
                        if (abs(view.x) < view.width / 2 && (event.rawX > rawTouchDownX)) {
                            animator(view, 0f, duration, unBlockingAnimatorListener(view as ViewGroup)).start()
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

    private fun activatingLoop(view : ViewGroup, isActive : Boolean) {
        for (i in 0 until view.childCount) {
            val child = view.getChildAt(i)
            child.isEnabled = isActive

            if(child is ViewGroup) {
                activatingLoop(child, isActive)
            }
        }
    }

    private fun blockingAnimatorListener(view : ViewGroup) =
        AnimatorListenerTemplate(
            onStart = {},
            onEnd = {
                activatingLoop(view, false)
                currentPosition = Position.Waiting
            }
        )

    private fun unBlockingAnimatorListener(view: ViewGroup) =
        AnimatorListenerTemplate(
            onStart = {},
            onEnd = {
                activatingLoop(view, true)
                currentPosition = Position.Start
            }
        )

}