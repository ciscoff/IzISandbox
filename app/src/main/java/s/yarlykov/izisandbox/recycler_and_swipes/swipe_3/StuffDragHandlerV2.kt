package s.yarlykov.izisandbox.recycler_and_swipes.swipe_3

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.LinearInterpolator
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.domain.EditorAction
import kotlin.math.abs

/**
 * Класс обрабатывает свайпы на доерах и ресурсах внутри карточек услуг
 */
open class StuffDragHandlerV1(private val view: View,
                              private val callback: (EditorAction) -> Unit) : View.OnTouchListener {
    private val context = view.context
    private var dX = 0f
    private var rawTouchDownX = 0f
    private val ratioOfNoReturn = 0.3f
    private var firstTouchDown = false
    private var touchDownTime = 0L

    private val durationToCenter = context.resources.getInteger(R.integer.animation_to_center_duration).toLong()
    private val durationOverEdge = context.resources.getInteger(R.integer.animation_over_edge_duration).toLong()

    private fun animator(shift: Float, animationDuration: Long, listener: Animator.AnimatorListener) =
        ObjectAnimator.ofFloat(view, "translationX", shift).apply {
            interpolator = LinearInterpolator()
            duration = animationDuration
            addListener(listener)
        }

    // При возврате в исходную позицию отправить сообщение EditorAction.SwipeToCenterEnd
    private val animateToCenterListener = AnimatorListenerTemplate({}, onEnd = {
        callback(EditorAction.SwipeToCenterEnd)
    })

    // При завершении "ухода" вправо нужно отправить сообщение EditorAction.SwipeToRight
    private val animateToRightListener = AnimatorListenerTemplate(
        onStart = {
            callback(EditorAction.SwipeToRightStart)
        },
        onEnd = {
            callback(EditorAction.SwipeToRightEnd)
        })

    // При завершении "ухода" влево нужно отправить сообщение EditorAction.SwipeToLeft
    private val animateToLeftListener = AnimatorListenerTemplate(
        onStart = {
            callback(EditorAction.SwipeToLeftStart)
        },
        onEnd = {
            callback(EditorAction.SwipeToLeftEnd)
        })


    // Отслеживает коодинату левой стороны View в момент предыдущего event'а.
    // Используется для того, чтобы определить смену направления движения пальца.
    private var lastLeft = 0f


    override fun onTouch(view: View, event: MotionEvent): Boolean {

        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {

                // Перенести в ACTION_MOVE. Иначе при первом касании блокируется и
                // горизонтальная и вертикальная прокрутка.
                view.parent.requestDisallowInterceptTouchEvent(true)

                touchDownTime = System.currentTimeMillis()
                firstTouchDown = true
                rawTouchDownX = event.rawX
                dX = view.x - event.rawX
                true
            }
            MotionEvent.ACTION_UP -> {

                touchDownTime = 0
                firstTouchDown = false

                view.parent.requestDisallowInterceptTouchEvent(false)
                val pointOfNoReturn = view.width * ratioOfNoReturn

                // Если сдвинули влево на дистанцию больше чем pointOfNoReturn,
                // то автоматически "дотягиваем" влево до конца.
                if (view.x < 0 && abs(view.x) > pointOfNoReturn) {
                    animator(-(view.width).toFloat(), durationOverEdge, animateToLeftListener).start()
                }
                // Если сдвинули вправо на дистанцию больше чем pointOfNoReturn,
                // то автоматически "дотягиваем" вправо до конца.
                else if (view.x > 0 && view.x > pointOfNoReturn) {
                    animator((view.width).toFloat(), durationOverEdge, animateToRightListener).start()
                }
                // Если не дотянули до pointOfNoReturn, то возвращаем View в исходное положение
                else {
                    animator(0f, durationToCenter, animateToCenterListener).start()
                }

                true
            }
            MotionEvent.ACTION_MOVE -> {

                if (movingIsStarted()) {
//                    view.parent.requestDisallowInterceptTouchEvent(true)

                    // Первое move после touchDown
                    if (firstTouchDown) {
                        callback(if (event.rawX > rawTouchDownX) EditorAction.DragToRight else EditorAction.DragToLeft)
                        lastLeft = if (event.rawX > rawTouchDownX) 0.1f else -0.1f
                        firstTouchDown = false
                    }
                    // Палец движется.
                    // По знаку произведения определяем пересекла ли слайдер-View левый край своей области.
                    // Цвет фона под слайдером будет меняться в обработчиках EditorAction.Drag... в
                    // зависимости от того в какую сторону пересечен этот край.
                    else if (view.x * lastLeft < 0) {
                        lastLeft = view.x
                        callback(if (view.x > 0) EditorAction.DragToRight else EditorAction.DragToLeft)
                    }

                    view.animate()
                        .x(event.rawX + dX)
                        .setDuration(0)
                        .start()
                    true

                } else {
                    false
                }
            }
            else -> {
                false
            }
        }
    }

    /**
     * Реагируем на ACTION_MOVE только после истечения системного интервала LongPressTimeout с момента
     * ACTION_DOWN. Нужно для того, чтобы не мешать вертикальной прокрутке списка услуг в заказе.
     */
    private fun movingIsStarted(): Boolean {
        return (touchDownTime > 0L) && (System.currentTimeMillis() - touchDownTime > ViewConfiguration.getLongPressTimeout() / 3
                )
    }
}