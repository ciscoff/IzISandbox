package s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d

/**
 * version : V4
 *
 * для работы с ScaleGestureListener V4 и обычным RecyclerView
 */
import android.graphics.Rect
import android.graphics.RectF
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.forceSiblingsToDo
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.holder.SmartCallback
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.model.Ticket
import s.yarlykov.izisandbox.utils.logIt
import kotlin.math.ceil

class ColumnTouchListener(
    private val view: View,
    private val ticket: Ticket,
    private val thresholdListener: SmartCallback<Float>
) : View.OnTouchListener {

    /**
     * Translate - перемещаем прямоугольник
     * Scale - скалируем прямоугольник
     * PullTop - тянем (вверх/вниз) за верхнюю границу прямоугольника
     * PullBottom - тянем (вверх/вниз) за нижнюю границу прямоугольника
     */
    enum class State {
        Translate,
        Scale,
        PullTop,
        PullBottom,
        Wait,
        Out
    }

    enum class TouchArea {
        LeftTop,
        BottomRight,
        Blue,
        Pink,
        Outside
    }

    var state: State = State.Wait
    private val context = view.context
    private val widthRatio = context.resources.getInteger(R.integer.touch_area_ratio)
    private val minInterval = context.resources.getInteger(R.integer.min_interval)

    /**
     * ppm - pixels per minute
     */
    private val ppm: Float
        get() {
            val dayRange = ticket.dayRange
            return (view.height).toFloat() / (dayRange.last - dayRange.first)
        }

    /**
     * Область выделения.
     * Координаты x/y в MotionEvent относительно View, а не родителя. Поэтому blueRect тоже должен
     * иметь координаты относительно View.
     */
    private val blueRect: RectF
        get() {
            val dayRange = ticket.dayRange

            return RectF(
                0f,
                (ticket.start - dayRange.first) * ppm,
                view.width.toFloat(),
                (ticket.end - dayRange.first) * ppm
            )
        }

    /**
     * Для обработки событий onTouch
     */
    private var activePointerId = 0
    private val pointers = mutableMapOf<Int, Float>()

    /**
     * Детектор zoom'а
     */
    private val scaleDetector =
        ScaleGestureDetector(context, ScaleGestureListener(view, ticket, ::state::set))

    /**
     * Зона клика.
     * Зоны клика над точками LT/BR - это квадраты со сторонами 2*w наполовину выходящие
     * за пределы blueRect.
     *
     * NOTE: View продолжает получать события (например ACTION_DOWN) даже если частично
     * находится под верхним/левым padding'ом RecyclerView и тач пришелся на это место.
     * В таком случае не нужно захватывать событие и возвращать true из ACTION_DOWN. Необходимо
     * возвращать false и иконка zoom'а будет работать корректно. Чтобы определить, что тач
     * пришелся в видимую область view поступаем так:
     * - getLocalVisibleRect соощит нам координаты видимой части view в координатах самой view
     * - event x/y тоже получаем в координатах самой view
     * Остается только проверить попадание event x/y в getLocalVisibleRect
     */
    private val MotionEvent.touchArea: TouchArea
        get() {

            // проверить, что тач в видимой области view
            val rect = Rect()
            view.getLocalVisibleRect(rect)
            if (!rect.contains(x.toInt(), y.toInt())) {
                return TouchArea.Outside
            }

            val area = blueRect
            val w = area.width() / widthRatio

            val lt = RectF(area.left, area.top - w, area.left + 2 * w, area.top + w)
            val br = RectF(area.right - 2 * w, area.bottom - w, area.right, area.bottom + w)

            return when {
                lt.contains(x, y) -> TouchArea.LeftTop
                br.contains(x, y) -> TouchArea.BottomRight
                area.contains(x, y) -> TouchArea.Blue
                else -> TouchArea.Pink
            }
        }

    /**
     * 1. Передать событие ScaleGestureDetector'у
     * 2. Самостоятельно продолжить обработку.
     *
     * ScaleGestureDetector не вносит изменений в событие и в процесс обработки. Его задача
     * "отловить" начало zoom'а, а затем обрабатывать прогресс каким-то действием. Здесь
     * меняется размер синего ползунка.
     */
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        return handleTouchEvent(view, event)
    }

    /**
     * Обработчик событий
     */
    private fun handleTouchEvent(view: View, event: MotionEvent): Boolean {

        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {

                pointers.clear()
                activePointerId = event.getPointerId(0)
                pointers[activePointerId] = event.getY(0)

                state = when (event.touchArea) {
                    TouchArea.BottomRight -> State.PullTop
                    TouchArea.LeftTop -> State.PullBottom
                    TouchArea.Blue -> State.Translate
                    TouchArea.Pink -> State.Wait
                    TouchArea.Outside -> State.Out
                }

                when (state) {
                    State.Translate, State.PullTop, State.PullBottom -> {
                        view.apply {
                            parent.requestDisallowInterceptTouchEvent(true)
                            forceSiblingsToDo { isSelected = false }
                            isSelected = true
                            (parent as ViewGroup).invalidate()
                        }
                        true
                    }
                    State.Wait -> true
                    else -> false
                }
            }

            // Касание вторым пальцем. Теперь оба пальца на экране.
            MotionEvent.ACTION_POINTER_DOWN -> {
                // TODO Нужно здесь блокировать Intercept у родителя, а не в onScaleBegin.
                view.parent.requestDisallowInterceptTouchEvent(true)

                event.actionIndex.also { index ->
                    pointers[event.getPointerId(index)] = event.getY(index)
                }
                true
            }

            // NOTE: Двигать blueRect только в состоянии Translating/PullDown/PullUp,
            // чтобы избежать неожиданных "прыжков".
            MotionEvent.ACTION_MOVE -> {

                if (state == State.Translate || state == State.PullTop || state == State.PullBottom) {
                    val y =
                        event.findPointerIndex(activePointerId).let { index -> event.getY(index) }

                    if (event.pointerCount == 1 && view.isSelected) {

                        when (state) {
                            State.Translate -> {
                                translate(y - pointers[activePointerId]!!, view)
                            }
                            State.PullTop -> {
                                pull(y - pointers[activePointerId]!!, view, TouchArea.BottomRight)
                            }
                            State.PullBottom -> {
                                pull(y - pointers[activePointerId]!!, view, TouchArea.LeftTop)
                            }
                        }

                        pointers[activePointerId] = y
                        return true
                    }
                }
                false
            }

            /**
             * Какой-то палец поднят (но не последний). Если это обладатель activePointerId, то нужно
             * оставшийся палец назначить на роль основного
             */
            MotionEvent.ACTION_POINTER_UP -> {
                event.actionIndex.also { index ->
                    event.getPointerId(index).takeIf { it == activePointerId }?.run {
                        val newIndex = index.xor(1) // (0 -> 1, 1 -> 0)
                        activePointerId = event.getPointerId(newIndex)
                        pointers[activePointerId] = event.getY(newIndex)
                    }
                }
                true
            }

            MotionEvent.ACTION_UP -> {
                view.parent.requestDisallowInterceptTouchEvent(false)
                state = State.Wait
                true
            }
            MotionEvent.ACTION_CANCEL -> {
                true
            }
            else -> false
        }
    }

    /**
     * Переместить blueRect вертикально. Проверить на границы view.
     */
    private fun translate(offset: Float, view: View) {
        val dayRange = ticket.dayRange

        blueRect.set(
            0f,
            (ticket.start - dayRange.first) * ppm,
            view.width.toFloat(),
            (ticket.end - dayRange.first) * ppm
        )

        // Обрабатываем крайние условия
        val dY = if (offset > 0) {
            if (blueRect.bottom + offset < view.height) offset else view.height - blueRect.bottom
        } else {
            if (blueRect.top + offset > 0) offset else -blueRect.top
        }

        val minutes = ceil(dY / ppm).toInt()

        ticket.start += minutes
        ticket.end += minutes
        state = State.Translate
        (view.parent as ViewGroup).invalidate()
    }

    /**
     * Тянем вверх, offset < 0
     * Тянем вниз, offset > 0
     */
    private fun pull(offset: Float, view: View, area: TouchArea) {

        val minMinutes = minInterval * ppm
        val minutes: Float
        val dY: Float

        var minThreshold = false

        // Это значения в PX относительно View
        val (start, end) = blueRect.let { it.top to it.bottom }

        when (area) {
            TouchArea.LeftTop -> {

                dY = when {
                    // Тянем вниз
                    (offset > 0 && ticket.end - ticket.start <= minInterval) -> return
                    // Тянем вверх
                    (offset < 0 && ticket.start == ticket.dayRange.first) -> return

                    // Тянем вниз и "перетягиваем" (offset > 0)
                    (start + offset >= (end - minMinutes)) -> {
                        minThreshold = true
                        end - start - minMinutes
                    }
                    // Тянем вверх и "перетягиваем" (offset < 0)
                    (start + offset < 0f) -> {
                        start
                    }
                    else -> offset
                }

                minutes = dY / ppm

                ticket.start += minutes.toInt()
                (view.parent as ViewGroup).invalidate()
            }
            TouchArea.BottomRight -> {

                dY = when {
                    // Тянем вниз
                    (offset > 0 && ticket.end == ticket.dayRange.last) -> return
                    // Тянем вверх
                    (offset < 0 && ticket.end - ticket.start <= minInterval) -> return

                    // Тянем вниз и "перетягиваем" (offset > 0)
                    (end + offset > view.height) -> {
                        view.height - end
                    }
                    // Тянем вверх и "перетягиваем" меньше нижнего значения(offset < 0)
                    (end + offset <= (start + minMinutes)) -> {
                        minThreshold = true
                        start - end + minMinutes
                    }
                    else -> offset
                }

                minutes = dY / ppm

                ticket.end += minutes.toInt()
                (view.parent as ViewGroup).invalidate()
            }
            else -> {
            }
        }

        // Сообщить Y-координату minThreshold'а
        if (minThreshold) {
            thresholdListener.call(start + (end - start) / 2)
        }
    }
}