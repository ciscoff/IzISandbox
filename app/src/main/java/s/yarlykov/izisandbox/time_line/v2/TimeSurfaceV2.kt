package s.yarlykov.izisandbox.time_line.v2

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import s.yarlykov.izisandbox.R
import kotlin.math.min
import kotlin.math.sign

/**
 * Алгоритм работы.
 *
 * При использовании нескольких указателей данные по каждому из них приходят в элементах
 * массива. Индекс указателя - индекс элемента массива. Однако индексы у указателей не сохраняются
 * от события к событию. То есть левый палец в одном событии может иметь индекс 0, а в следующем
 * уже 1. Однако у указателей есть уникальные ID и они гарантированно уникальны. Поэтому приходится
 * мапить индексы в указатели.
 *
 * Основная трабла в том как правильно организовать масштабирование. Итак, используем две Map'ы.
 * pointers - ключём является pointer_id и храним последние X для каждого указателя, points -
 * хранит последние Х для ЛЕВОГО и ПРАВОГО указателей. Её пофиг на ID. Её задача различать
 * левый и правый и это нужно для масштабирования. Масштабирование начинается в момент фиксации
 * Direction.Opposite. Как только указатели начали удаляться или сближаться, то начинается
 * масштабирование. При фиксации Direction.Same оно заказнчивается.
 *
 * NOTE: Сохранять координаты указателей нужно постоянно, чтобы ползунок не прыгал при отпускании
 * пальца.
 *
 * NOTE: Использовать совместно ScaleGestureDetector.SimpleOnScaleGestureListener и свой обработчик
 * onTouchEvent не получится, потому что ScaleGestureDetector всегда возвращает true и нет
 * возможности забрать у него другие события.
 */
class TimeSurfaceV2 : ViewGroup {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setWillNotDraw(false)
    }

    companion object {
        const val INVALID_POINTER_ID = -1
    }

    private enum class Direction {
        Same,
        Opposite
    }

    private enum class Pointer {
        Left,
        Right
    }

    private var activePointerId = 0
    private var frameX = 0f
    private var scaleFactor = 1f

    private val pointers = mutableMapOf<Int, Float>()
    private val points = mutableMapOf(Pointer.Left to 0f, Pointer.Right to 0f)

    private lateinit var timeFrame: TimeFrameV2

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setBackgroundColor(
            ContextCompat.getColor(
                context, R.color.colorDecor14
            )
        )
    }

    /**
     * У меня пока размеры задаются жестко. Поэтому вариант WRAP_CONTENT не учитываем.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val (rw, rh) = MeasureSpec.getSize(widthMeasureSpec) to
                MeasureSpec.getSize(heightMeasureSpec)

        val (childX, childY) = 0 to 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)

            measureChild(child, widthMeasureSpec, heightMeasureSpec)

            if (child is TimeFrameV2) {
                timeFrame = child
                val params = timeFrame.layoutParams as LayoutParams

                params.x = childX
                params.y = childY
            }
        }

        setMeasuredDimension(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val params = child.layoutParams as LayoutParams
            child.layout(params.x, params.y, child.measuredWidth, child.measuredHeight)
        }
        translateFrame(0f)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.actionMasked) {

            // Самое первое касание в жесте.
            MotionEvent.ACTION_DOWN -> {
                pointers.clear()
                activePointerId = event.getPointerId(0)
                pointers[activePointerId] = event.getX(0)
            }

            // Касание вторым пальцем. Теперь оба пальца на экране.
            MotionEvent.ACTION_POINTER_DOWN -> {
                event.actionIndex.also { index ->
                    pointers[event.getPointerId(index)] = event.getX(index)
                }
                savePoints(event)
            }

            // Уже в процессе. Мы все время ориентируемся на основной pointer, который
            // отслеживается по activePointerId
            MotionEvent.ACTION_MOVE -> {
                val x = event.findPointerIndex(activePointerId)
                    .let { index -> event.getX(index) }

                when (event.pointerCount) {
                    1 -> {
                        translateFrame(x - pointers[activePointerId]!!)
                        pointers[activePointerId] = x
                    }
                    2 -> {
                        when (direction(event)) {
                            Direction.Same -> {
                                translateFrame(x - pointers[activePointerId]!!)
                                savePointers(event)
                                savePoints(event)
                            }
                            Direction.Opposite -> {
                                resizeFrame(event)
                                savePointers(event)
                                savePoints(event)
                            }
                        }
                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activePointerId =
                    INVALID_POINTER_ID
            }

            /**
             * Какой-то палец поднят (но не последний). Если это обладатель activePointerId, то нужно
             * оставшийся палец назначить на роль основного
             */
            MotionEvent.ACTION_POINTER_UP -> {
                event.actionIndex.also { index ->
                    event.getPointerId(index).takeIf { it == activePointerId }?.run {
                        val newIndex = if (index == 0) 1 else 0
                        activePointerId = event.getPointerId(newIndex)
                        pointers[activePointerId] = event.getX(newIndex)
                    }
                }
            }
        }

        return true
    }

    /**
     * Определить направление движения пальцев: в одном направлении или в противоположном.
     * Если направление противоположное, то это scale. Направление считается противоположным и в
     * случае, если один палец на месте, а второй двигается.
     */
    private fun direction(event: MotionEvent): Direction {
        val (indexL, indexR) = pointsIndices(event)

        val (currentL, prevL) = event.getX(indexL) to pointers[event.getPointerId(indexL)]!!
        val (currentR, prevR) = event.getX(indexR) to pointers[event.getPointerId(indexR)]!!

        return if (sign((currentL - prevL) * (currentR - prevR)) <= 0) Direction.Opposite else Direction.Same
    }

    /**
     * Переместить ползунок
     */
    private fun translateFrame(dX: Float) {

        if (!this::timeFrame.isInitialized) return

        frameX += dX

        /**
         * Ползунок не должен вылезать за края родительского элемента
         */
        when {
            (frameX < 0) -> {
                frameX = 0f
            }
            (frameX > measuredWidth - timeFrame.measuredWidth) -> {
                frameX = (measuredWidth - timeFrame.measuredWidth).toFloat()
            }
        }

        timeFrame.translationX = frameX
    }

    /**
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

    /**
     * В этом методе важно различать левый и правый указатели.
     * Active/Passive роли не играет
     */
    private fun resizeFrame(event: MotionEvent) {

        val (indexL, indexR) = pointsIndices(event)
        val (xL, xR) = event.getX(indexL) to event.getX(indexR)

        val prevSpan = points[Pointer.Right]!! - points[Pointer.Left]!!
        val currentSpan = xR - xL

        val factor = currentSpan / prevSpan

        scaleFactor *= factor

        val frameWidthBefore = timeFrame.measuredWidth
        // Ползунок не может быть шире родителя
        val frameWidthAfter = min((frameWidthBefore * factor).toInt(), (timeFrame.parent as ViewGroup).measuredWidth)

        // Нужно подвинуть левый край левее на половину изменения ширины
        val tX = (frameWidthAfter - frameWidthBefore) / 2f
        translateFrame(-tX)

        timeFrame.layoutParams = timeFrame.layoutParams.apply { width = frameWidthAfter }
    }

    /**
     * Наш контейнер должен генерить LayoutParams для детей
     */
    override fun generateLayoutParams(attrs: AttributeSet?): ViewGroup.LayoutParams {
        return LayoutParams(
            context,
            attrs
        )
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
        return LayoutParams(p)
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        return p is LayoutParams
    }

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return LayoutParams()
    }

    /**
     * Кастомные layoutParams
     */
    class LayoutParams : ViewGroup.LayoutParams {

        var x = 0
        var y = 0

        @JvmOverloads
        constructor(
            width: Int = MATCH_PARENT,
            height: Int = MATCH_PARENT
        ) : super(width, height)

        constructor(context: Context?, attrs: AttributeSet?) : super(
            context,
            attrs
        )

        constructor(params: ViewGroup.LayoutParams) : super(params)
    }
}