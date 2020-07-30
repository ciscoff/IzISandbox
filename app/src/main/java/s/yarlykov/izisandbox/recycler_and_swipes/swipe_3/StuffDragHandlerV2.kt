package s.yarlykov.izisandbox.recycler_and_swipes.swipe_3

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.LinearInterpolator
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.globLeft
import s.yarlykov.izisandbox.recycler_and_swipes.animation.AnimatorListenerTemplate
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.domain.EditorAction

/**
 * Класс обрабатывает свайпы на доерах и ресурсах внутри карточек услуг.
 * Его задача следить за движением пальца, анимировать перемещения ползунка
 * и через callback сообщать о завершении той или иной анимации.
 *
 * callback, созданный в SliderCallbackFactory, конвертирует сообщения о завершении
 * анимации в сообщения о требуемых действиях и кидает дальше в фрагмент.
 */
open class StuffDragHandlerV2(
    private val view: View,
    private val callback: (EditorAction) -> Unit
) : View.OnTouchListener {

    enum class SliderState {
        Below,
        Above
    }

    private val context = view.context
    private var dX = 0f
    private var rawTouchDownX = 0f
    private val ratioOfNoReturn = 0.15f /*0.3f*/
    private var firstTouchDown = false
    private var touchDownTime = 0L

    private val durationToCenter =
        context.resources.getInteger(R.integer.animation_to_center_duration).toLong()
    private val durationOverEdge =
        context.resources.getInteger(R.integer.animation_over_edge_duration).toLong()

    // Отслеживаем состояние левого края - до или после границы noReturn
    private var state = SliderState.Below

    private fun animator(
        shift: Float,
        animationDuration: Long,
        listener: Animator.AnimatorListener
    ) =
        ObjectAnimator.ofFloat(view, "translationX", shift).apply {
            interpolator = LinearInterpolator()
            duration = animationDuration
            addListener(listener)
        }

    // При возврате в исходную позицию отправить сообщение EditorAction.SwipeToCenterEnd
    private val animateToCenterListener =
        AnimatorListenerTemplate(
            {},
            onEnd = {
                callback(EditorAction.SwipeToCenterEnded)
            })

    // При завершении "ухода" вправо нужно отправить сообщение EditorAction.SwipeToRight
    private val animateToRightListener =
        AnimatorListenerTemplate(
            onStart = {
                callback(EditorAction.SwipeToRightStarted)
            },
            onEnd = {
                callback(EditorAction.SwipeToRightEnded)
            })

    // При завершении "ухода" влево нужно отправить сообщение EditorAction.SwipeToLeft
    private val animateToLeftListener =
        AnimatorListenerTemplate(
            onStart = {
                callback(EditorAction.SwipeToLeftStarted)
            },
            onEnd = {
                callback(EditorAction.SwipeToLeftEnded)
            })

    override fun onTouch(view: View, event: MotionEvent): Boolean {

        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {

                // Перенести в ACTION_MOVE. Иначе при первом касании блокируется и
                // горизонтальная и вертикальная прокрутка.
                view.parent.requestDisallowInterceptTouchEvent(true)

                touchDownTime = System.currentTimeMillis()
                firstTouchDown = true
                state = SliderState.Below
                rawTouchDownX = event.rawX
                dX = view.globLeft - event.rawX
                true
            }
            MotionEvent.ACTION_UP -> {

                touchDownTime = 0
                firstTouchDown = false

                view.parent.requestDisallowInterceptTouchEvent(false)
                val pointOfNoReturn = view.width * ratioOfNoReturn

                // Если сдвинули вправо на дистанцию больше чем pointOfNoReturn,
                // то автоматически "дотягиваем" вправо до конца.
                if (view.x > pointOfNoReturn) {
                    animator(
                        (view.width).toFloat(),
                        durationOverEdge,
                        animateToRightListener
                    ).start()
                }
                // Если не дотянули до pointOfNoReturn, то возвращаем View в исходное положение
                else {
                    animator(0f, durationToCenter, animateToCenterListener).start()
                }

                true
            }
            MotionEvent.ACTION_MOVE -> {

                if (movingToRight(event)) {
//                    view.parent.requestDisallowInterceptTouchEvent(true)

                    val pointOfNoReturn = view.width * ratioOfNoReturn
                    val offset = event.rawX + dX

                    when {
                        // Первое move после touchDown
                        firstTouchDown -> {
                            callback(EditorAction.DragToRight)
                            firstTouchDown = false
                        }
                        // Двигаемся вправо и пересекаем линию noReturn
                        (offset >= pointOfNoReturn && state == SliderState.Below) -> {
                            state = SliderState.Above
                            callback(EditorAction.SwipeToAbove)
                        }
                        (offset < pointOfNoReturn && state == SliderState.Above) -> {
                            state = SliderState.Below
                            callback(EditorAction.SwipeToBelow)
                        }
                    }

                    view.animate()
                        .x(offset)
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
        return (touchDownTime > 0L) && (System.currentTimeMillis() - touchDownTime > ViewConfiguration.getLongPressTimeout()/3)
    }

    /**
     * Эта функция блокирует свайп справо налево. Для доеров и ресурсов это означает, что их нельзя
     * удалить свайпом, а только через иконку корзинки. Для работы без ограничений нужно использовать
     * версию movingIsStarted()
     */
    private fun movingToRight(event: MotionEvent): Boolean {
        return movingIsStarted() && event.rawX > rawTouchDownX
    }
}