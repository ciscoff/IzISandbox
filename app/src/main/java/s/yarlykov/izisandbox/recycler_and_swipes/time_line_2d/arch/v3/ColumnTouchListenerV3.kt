package s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.arch.v3

/**
 * version : V3
 *
 * для работы с ScaleGestureListenerV3 и кастомным ContainerView : RecyclerView
 */
import android.graphics.RectF
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.forceSiblingsToDo
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.model.Ticket
import s.yarlykov.izisandbox.utils.logIt
import kotlin.math.ceil

class ColumnTouchListenerV3(
    private val view: View,
    private val ticket: Ticket
) : View.OnTouchListener {

    enum class State {
        Translate,
        Scale,
        None
    }

    enum class Area {
        LeftTop,
        BottomRight,
        Inside,
        Outside
    }

    /**
     * Палец левый/правый
     */
    private enum class Pointer {
        Left,
        Right
    }

    var state: State = State.None
    private val context = view.context
    private val widthRatio = context.resources.getInteger(R.integer.touch_area_ratio)

    /**
     * Область выделения.
     * Координаты (x/y) в MotionEvent относительно родителя. Поэтому blueRect тоже должен
     * иметь координаты относительно родителя.
     */
    private val blueRect: RectF
        get() {
            val dayRange = ticket.dayRange

            // ppm - pixels per minute
            val ppm = (view.height).toFloat() / (dayRange.last - dayRange.first)

            return RectF(
                view.left.toFloat(),
                view.top + (ticket.start - dayRange.first) * ppm,
                view.right.toFloat(),
                view.top + (ticket.end - dayRange.first) * ppm
            )
        }

    /**
     * Для обработки событий onTouch
     */
    private var activePointerId = 0
    private val pointers = mutableMapOf<Int, Float>()
    private val points = mutableMapOf(Pointer.Left to 0f, Pointer.Right to 0f) // TODO ??

    /**
     * Детектор zoom'а
     */
    private val scaleDetector =
        ScaleGestureDetector(context, ScaleGestureListenerV3(view, ticket, ::state::set))

    /**
     * Зона клика.
     * Зоны клика над точками LT/BR - это квадраты со сторонами 2*w наполовину выходящие
     * за пределы blueRect.
     */
    private val MotionEvent.touchArea: Area
        get() {

            val area = blueRect
            val w = area.width() / widthRatio

            val lt = RectF(area.left, area.top - w, area.left + 2 * w, area.top + w)
            val br = RectF(area.right - 2 * w, area.bottom - w, area.right, area.bottom + w)

            return when {
                lt.contains(x, y) -> Area.LeftTop
                br.contains(x, y) -> Area.BottomRight
                area.contains(x, y) -> Area.Inside
                else -> Area.Outside
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
        handleTouchEvent(view, event)
        return true
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

                when (event.touchArea) {
                    Area.BottomRight -> logIt("Area BR")
                    Area.LeftTop -> logIt("Area LT")
                    Area.Outside -> logIt("Area Outside")
                    Area.Inside -> logIt("Area Inside")
                }

                if (insideBlueRegion(event, view)) {
                    state = State.Translate
                    view.apply {
                        parent.requestDisallowInterceptTouchEvent(true)
                        forceSiblingsToDo { isSelected = false }
                        isSelected = true
                        (parent as ViewGroup).invalidate()
                    }
                }
                true
            }

            // Касание вторым пальцем. Теперь оба пальца на экране.
            MotionEvent.ACTION_POINTER_DOWN -> {
                event.actionIndex.also { index ->
                    pointers[event.getPointerId(index)] = event.getY(index)
                }
                savePoints(event)   // TODO ??
                true
            }

            // NOTE: Двигать blueRect только в состоянии Translating, чтобы измежать
            // неожиданных прыжков.
            MotionEvent.ACTION_MOVE -> {
                if (state == State.Translate) {
                    val y =
                        event.findPointerIndex(activePointerId).let { index -> event.getY(index) }

                    if (event.pointerCount == 1 && view.isSelected) {
                        translateBlueRect(y - pointers[activePointerId]!!, view)
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
                state = State.None
                true
            }
            MotionEvent.ACTION_CANCEL -> {
                true
            }
            else -> false
        }
    }

    /**
     * Попробовать захватить событие одиночного тача. Для этого нужно определить положение
     * blueRect и проверить, что координаты event'а попадают в него. Значения event.x/y определены
     * в координатах РОДИТЕЛЯ, поэтому blueRect тоже создается в координатах РОДИТЕЛЯ (в декораторах
     * тоже используются координаты относительно родителя потому что там отрисовка на канве
     * родителя)
     */
    private fun insideBlueRegion(event: MotionEvent, view: View): Boolean {
        return blueRect.contains(event.x, event.y)
    }

    /**
     * Переместить blueRect вертикально. Проверить на границы view.
     */
    private fun translateBlueRect(offset: Float, view: View) {
        val dayRange = ticket.dayRange

        // ppm - pixels per minute
        val ppm = (view.height).toFloat() / (dayRange.last - dayRange.first)

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
     * TODO ??
     * Сохранить координаты левого и правого указателей.
     *
     * Функция исползует содержимое структуры pointers, поэтому перед её вызовом необходимо
     * поместить в pointers последнюю актуальную информацию (savePointers)
     */
    private fun savePoints(event: MotionEvent) {
        val (l, r) = pointsIndices(event)

        points[Pointer.Left] = event.getX(l)
        points[Pointer.Right] = event.getX(r)
    }

    /**
     * TODO ??
     * Сохранить координаты активного и пассивного указателей
     */
    private fun savePointers(event: MotionEvent) {
        val (a, p) = pointersIndices(event)

        val passivePointerId = event.getPointerId(p)
        pointers[activePointerId] = event.getX(a)
        pointers[passivePointerId] = event.getX(p)
    }

    /**
     * Определить индексы активного и пассивного указателей
     */
    private fun pointersIndices(event: MotionEvent): Pair<Int, Int> {
        val indexActive = event.findPointerIndex(activePointerId)
        val indexPassive = if (indexActive == 0) 1 else 0

        return indexActive to indexPassive
    }

    /**
     * Определить индексы левого и правого указателей
     */
    private fun pointsIndices(event: MotionEvent): Pair<Int, Int> {
        val lastTouch0 = pointers[event.getPointerId(0)]!!
        val lastTouch1 = pointers[event.getPointerId(1)]!!

        val indexL = if (lastTouch0 < lastTouch1) 0 else 1
        val indexR = if (lastTouch0 < lastTouch1) 1 else 0

        return indexL to indexR
    }
}