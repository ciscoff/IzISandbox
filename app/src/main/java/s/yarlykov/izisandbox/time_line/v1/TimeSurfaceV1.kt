package s.yarlykov.izisandbox.time_line.v1

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.Utils.logIt
import kotlin.math.abs
import kotlin.math.sign

class TimeSurfaceV1 : ViewGroup {
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

    lateinit var timeFrame: TimeFrameV1

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
        var (childX, childY) = 0 to 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)

            measureChild(child, widthMeasureSpec, heightMeasureSpec)

            if (child is TimeFrameV1) {
                timeFrame = child
                val params = timeFrame.layoutParams as TimeSurfaceV1.LayoutParams

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
            val params = child.layoutParams as TimeSurfaceV1.LayoutParams
            child.layout(params.x, params.y, child.measuredWidth, child.measuredHeight)
        }
        translateFrame()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    private var activePointerId = 0
    private var widthBeforeTouch = 0
    private var translationBeforeTouch = 0f
    private var frameX = 0f

    private val pointers = mutableMapOf<Int, Float>()

    private var isScaling = false

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.actionMasked) {

            // Самое первое касание в жесте.
            MotionEvent.ACTION_DOWN -> {
                event.actionIndex.also { index ->
                    activePointerId = event.getPointerId(index)
                    pointers[activePointerId] = event.getX(index)
                }
            }

            // Касание вторым пальцем. Теперь оба пальца на экране.
            MotionEvent.ACTION_POINTER_DOWN -> {
                event.actionIndex.also { index ->
                    pointers[event.getPointerId(index)] = event.getX(index)
                    widthBeforeTouch = timeFrame.measuredWidth
                    translationBeforeTouch = timeFrame.translationX
                }
            }

            // Уже в процессе. Мы все время ориентируемся на основной pointer, который
            // отслеживается по activePointerId
            MotionEvent.ACTION_MOVE -> {
                val x = event.findPointerIndex(activePointerId)
                    .let { index -> event.getX(index) }

                when (event.pointerCount) {
                    1 -> {
                        frameX += x - pointers[activePointerId]!!
                        translateFrame()
                        pointers[activePointerId] = x
                    }
                    2 -> {
                        when (direction(event)) {
                            Direction.Same -> {
                                isScaling = false


//                                logIt("Direction is ${Direction.Same}", "GOGO")
//                                frameX += x - pointers[activePointerId]!!
//                                translateFrame()
//                                pointers[activePointerId] = x



                                if(!isScaling) {
                                    logIt("Direction is ${Direction.Same}", "GOGO")
                                    frameX += x - pointers[activePointerId]!!
                                    translateFrame()
                                    pointers[activePointerId] = x
                                } /*else {
                                    frameX += x - pointers[activePointerId]!!
                                }*/
                            }
                            Direction.Opposite -> {
//                                logIt("Direction is ${Direction.Opposite}", "GOGO")
                                isScaling = true
                                resizeFrame(event)
                            }
                        }

                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activePointerId = INVALID_POINTER_ID
            }

            /**
             * Какой-то палец поднят (но не последний). Если это обладатель activePointerId, то нужно
             * оставшийся палец назначить на роль основного
             */
            MotionEvent.ACTION_POINTER_UP -> {
                logIt("ACTION_POINTER_UP", "GOGO")
                isScaling = false

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
    private var prevDir : Direction = Direction.Same

    private fun direction(event: MotionEvent): Direction {

        val lastTouch0 = pointers[event.getPointerId(0)]!!
        val lastTouch1 = pointers[event.getPointerId(1)]!!

        val indexL = if(lastTouch0 < lastTouch1) 0 else 1
        val indexR = if(lastTouch0 < lastTouch1) 1 else 0

        val (xL, lastTouchL) = event.getX(indexL) to pointers[event.getPointerId(indexL)]!!
        val (xR, lastTouchR) = event.getX(indexR) to pointers[event.getPointerId(indexR)]!!

//        if(abs(xL - lastTouchL) > 10 && abs(xR - lastTouchR) > 10) {
//            prevDir = if (sign((xL - lastTouchL) * (xR - lastTouchR)) > 0) Direction.Same else Direction.Opposite
//        }
//        return prevDir
        return if (sign((xL - lastTouchL) * (xR - lastTouchR)) > 0) Direction.Same else Direction.Opposite
    }

    private fun translateFrame() {

        if (!this::timeFrame.isInitialized) return

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

    private fun resizeFrame(event: MotionEvent) {
        val indexActive = event.findPointerIndex(activePointerId)
        val indexPassive = if(indexActive == 0) 1 else 0

        val touchActive = pointers[activePointerId]!!
        val touchPassive = pointers[event.findPointerIndex(indexPassive)]!!

        val xActive = event.getX(indexActive)
        val xPassive = event.getX(indexPassive)

        val gestureWidthBefore = abs(touchActive - touchPassive)
        val gestureWidthAfter = abs(xActive - xPassive)
        val ratio = gestureWidthAfter/gestureWidthBefore

        val frameWidthBefore = timeFrame.measuredWidth
        val frameWidthAfter = (widthBeforeTouch * ratio).toInt()

        val tX = (frameWidthAfter - frameWidthBefore) / 2
        frameX -= tX

        timeFrame.layoutParams = timeFrame.layoutParams.apply {
            width = frameWidthAfter
//            logIt("indexL=$indexL, indexR=$indexR, xL=$xL, lastTouchL=$lastTouchL, xR=$xR, lastTouchR=$lastTouchR, translation=${timeFrame.translationX}, dW=$dW, width=${widthBeforeTouch + dW.toInt()}", "GOGO")
        }

//        timeFrame.postDelayed({translateFrame()}, 10)

    }

    private fun resizeFrameV1(event: MotionEvent) {

        val lastTouch0 = pointers[event.getPointerId(0)]!!
        val lastTouch1 = pointers[event.getPointerId(1)]!!

        val indexL = if(lastTouch0 < lastTouch1) 0 else 1
        val indexR = if(lastTouch0 < lastTouch1) 1 else 0

        val (xL, lastTouchL) = event.getX(indexL) to pointers[event.getPointerId(indexL)]!!
        val (xR, lastTouchR) = event.getX(indexR) to pointers[event.getPointerId(indexR)]!!

        val dWl = xL - lastTouchL
        val dWr = xR - lastTouchR

        frameX = translationBeforeTouch + dWl

//        timeFrame.translationX = frameX

        val dW = if(dWl < 0 || dWr > 0) (abs(dWl) + dWr) else -(dWl + abs(dWr))

        timeFrame.layoutParams = timeFrame.layoutParams.apply {
            width = widthBeforeTouch + dW.toInt()
//            logIt("indexL=$indexL, indexR=$indexR, xL=$xL, lastTouchL=$lastTouchL, xR=$xR, lastTouchR=$lastTouchR, translation=${timeFrame.translationX}, dW=$dW, width=${widthBeforeTouch + dW.toInt()}", "GOGO")
        }

        timeFrame.postDelayed({timeFrame.translationX = frameX}, 100)
    }

    /**
     * Наш контейнер должен генерить LayoutParams для детей
     */

    override fun generateLayoutParams(attrs: AttributeSet?): ViewGroup.LayoutParams {
        return TimeSurfaceV1.LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
        return TimeSurfaceV1.LayoutParams(p)
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        return p is TimeSurfaceV1.LayoutParams
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