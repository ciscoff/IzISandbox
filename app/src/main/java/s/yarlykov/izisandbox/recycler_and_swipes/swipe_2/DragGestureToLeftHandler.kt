package s.yarlykov.izisandbox.recycler_and_swipes.swipe_2

import android.animation.Animator
import android.animation.ObjectAnimator
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import s.yarlykov.izisandbox.utils.logIt
import s.yarlykov.izisandbox.recycler_and_swipes.helpers.LocalAnimatorListener
import s.yarlykov.izisandbox.recycler_and_swipes.helpers.LongPress
import kotlin.math.abs

class DragGestureToLeftHandler(private val view: View) : View.OnTouchListener {

    enum class Position {
        Start,
        Waiting
    }

    private var dX = 0f
    private var rawTouchDownX = 0f
    private var firstTouchDown = false
    private val duration = 200L

    private var viewGlobalX = 0

    private var currentPosition = Position.Start

    private fun animator(shift: Float, animationDuration: Long, listener: Animator.AnimatorListener) =
        ObjectAnimator.ofFloat(view, "translationX", shift).apply {
            interpolator = LinearInterpolator()
            duration = animationDuration
            addListener(listener)
        }

    private val animateToWaitPosition = LocalAnimatorListener(
        onStart = {

        },
        onEnd = {
            currentPosition = Position.Waiting
            logIt("set position to $currentPosition")
        })

    private val animateToStartPosition = LocalAnimatorListener(
        onStart = {

        },
        onEnd = {
            currentPosition = Position.Start
            logIt("set position to $currentPosition")
        })

    override fun onTouch(v: View, event: MotionEvent): Boolean {

        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                logIt("ACTION_DOWN")

                LongPress.restart()

                v.parent.requestDisallowInterceptTouchEvent(true)

                val rect = Rect()
                view.getGlobalVisibleRect(rect)
                viewGlobalX = rect.left

                if(event.rawY < rect.top + view.height) {
                    rawTouchDownX = event.rawX
                    firstTouchDown = true
                    dX = v.x - event.rawX
                    true
                } else false
            }
            MotionEvent.ACTION_MOVE -> {
                logIt("Enter in ACTION_MOVE")

                if(LongPress.isOccurred) {
                    logIt("Start moving ACTION_MOVE")
                    v.parent.requestDisallowInterceptTouchEvent(true)

                    when(currentPosition) {
                        Position.Start -> {
                            if(event.rawX < rawTouchDownX && abs(view.x) < view.width/2) {
                                v.animate()
                                    .x(event.rawX + dX)
                                    .setDuration(0)
                                    .start()
                            }
                        }
                        Position.Waiting -> {
                            if(event.rawX > rawTouchDownX && view.x <= 0) {

                                /**
                                 * event.rawX - это абс координаты относительно границ экрана.
                                 *
                                 * Вот тут по смещениям:
                                 * https://github.com/ciscoff/CircleRecycler/blob/stage_03/app/src/main/java/s/yzrlykov/circlerecycler/stages/s03_1/Activity03Layouts.kt
                                 *
                                 * v.animate().x - это НЕ АБСОЛЮТНОЕ ЗНАЧЕНИЕ, а знаковое смещение от
                                 * базового, установленного в фазе layout. То есть анимация заключается
                                 * в том, чтобы менять это смещение относительно базовой позиции.
                                 *
                                 * Если shift отрицательный, то картинка уйдет влево за экран. Чтобы
                                 * вернуть её обратно в положительную область, используем abs(shift).
                                 */
                                val shift = event.rawX + dX
//                            logIt("view.x=${v.x}, event.rawX=${event.rawX}, dX=$dX, rawTouchDownX=$rawTouchDownX, shift=$shift")

                                v.animate()
                                    .x(shift)
                                    .setDuration(0)
                                    .start()
                            }
                        }
                    }
                    true
                } else {
                    logIt("Ignoring ACTION_MOVE")
                    v.parent.requestDisallowInterceptTouchEvent(false)
                    false
                }
            }
            MotionEvent.ACTION_UP -> {
                logIt("ACTION_UP")
                v.parent.requestDisallowInterceptTouchEvent(false)

                if(LongPress.isOccurred) {
                    when(currentPosition) {
                        Position.Start -> {
                            logIt("abs(view.x)=${abs(view.x)}, event.rawX=${event.rawX}, rawTouchDownX=$rawTouchDownX")
                            if(abs(view.x) < view.width && event.rawX != rawTouchDownX) {
                                animator(-(view.width/2).toFloat(), duration, animateToWaitPosition).start()
                            }
                        }
                        Position.Waiting -> {
                            if(abs(view.x) < view.width/2 && (event.rawX > rawTouchDownX)) {
                                animator(0f, duration, animateToStartPosition).start()
                            }
                        }
                        else -> {
                            animator(0f, duration, animateToStartPosition).start()
                        }
                    }

                    firstTouchDown = false
                    rawTouchDownX = 0f
                    LongPress.reset()
                    true
                } else {
                    LongPress.reset()
                    false
                }
            }
            // Unknown MotionEvent
            else -> {
                false
            }
        }
    }
}