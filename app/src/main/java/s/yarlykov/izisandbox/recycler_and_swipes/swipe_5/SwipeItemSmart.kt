package s.yarlykov.izisandbox.recycler_and_swipes.swipe_5

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.Utils.logIt
import s.yarlykov.izisandbox.dsl.extenstions.*
import s.yarlykov.izisandbox.dsl.frameLayout
import s.yarlykov.izisandbox.dsl.frameLayoutParams
import s.yarlykov.izisandbox.dsl.textView
import kotlin.math.abs
import kotlin.math.sign

private const val TAG_SWIPE = "TAG_SWIPE"

const val noId = 0
const val positionLeft = 1000
const val positionRight = 2000

class SwipeItemSmart : FrameLayout, View.OnClickListener {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setUpAttrs(attrs)
        setupNestedViews()
    }

    private lateinit var itemView: View

    /**
     * Набор переменных, инициализируемых через кастомные атрибуты в XML
     */
    private var leftFrame: Boolean = false
    private var rightFrame: Boolean = false

    private var itemLayoutId: Int = noId
    private var rightColorsRes: Int = noId
    private var leftColorsRes: Int = noId
    private var leftTextRes: Int = noId
    private var rightTextRes: Int = noId

    private var leftStrings = arrayListOf<String>()
    private var rightStrings = arrayListOf<String>()
    private var leftColors = arrayListOf<Int>()
    private var rightColors = arrayListOf<Int>()
    private var leftViews = mutableListOf<View>()
    private var rightViews = mutableListOf<View>()

    /**
     * Аниматор для перемещения боковых элементов
     */
    private fun animator(
        view: View,
        shift: Float,
        animationDuration: Long,
        listener: Animator.AnimatorListener
    ) = ObjectAnimator.ofFloat(view, "x", shift).apply {
        interpolator = LinearInterpolator()
        duration = animationDuration
        addListener(listener)
    }

    private val animateToStartPosition = AnimatorListenerTemplate(
        onStart = {
        },
        onEnd = {
            currentState = State.Start
        })

    private val animateToWaitingPosition = AnimatorListenerTemplate(
        onStart = {
        },
        onEnd = {
            currentState = State.Waiting
        })

    enum class State {
        Start,
        Waiting
    }

    /**
     * Переменные для работы со свайпом
     */
    private var rawTouchDownX = 0f
    private var rawTouchDownY = 0f
    private val duration = 250L

    private var viewGlobalX = 0
    private var touchSlop = 0

    private var currentState = State.Start

    /**
     * Прочитать атрибуты, инициализировать переменные класса
     */
    private fun setUpAttrs(attrs: AttributeSet?) {

        context.obtainStyledAttributes(attrs, R.styleable.SwipeItemSmart).apply {

            itemLayoutId = getResourceId(R.styleable.SwipeItemSmart_layoutId, noId)
            leftFrame = getBoolean(R.styleable.SwipeItemSmart_leftFrame, false)
            rightFrame = getBoolean(R.styleable.SwipeItemSmart_rightFrame, false)
            leftColorsRes = getResourceId(R.styleable.SwipeItemSmart_leftColors, noId)
            rightColorsRes = getResourceId(R.styleable.SwipeItemSmart_rightColors, noId)
            leftTextRes = getResourceId(R.styleable.SwipeItemSmart_leftStrings, noId)
            rightTextRes = getResourceId(R.styleable.SwipeItemSmart_rightStrings, noId)
        }.recycle()

        if (leftFrame && leftColorsRes != noId && leftTextRes != noId) {
            leftStrings.addAll(resources.getStringArray(leftTextRes))
            leftColors.addAll(resources.getIntArray(leftColorsRes).toTypedArray())
        }

        if (rightFrame && leftColorsRes != noId && rightTextRes != noId) {
            rightStrings.addAll(resources.getStringArray(rightTextRes))
            rightColors.addAll(resources.getIntArray(rightColorsRes).toTypedArray())
        }
    }

    /**
     * Добавить View основного элемента. Добавить Views боковых элементов.
     */
    private fun setupNestedViews() {
        compareArrays(leftStrings, leftColors)
        compareArrays(rightStrings, rightColors)


        if (itemLayoutId != noId) {
            itemView = LayoutInflater.from(context).inflate(itemLayoutId, null)

            addView(itemView)
            addSideViews(positionLeft)
            addSideViews(positionRight)
            itemView.bringToFront()
        }
    }

    /**
     * Добавить боковые элементы (это контейнеры FrameLayout с вложенными TextView)
     */
    private fun addSideViews(pos: Int) {

        // Цвет фона и текст для каждой TextView
        val (strings, colors) =
            if (pos == positionLeft) {
                leftStrings to leftColors
            } else {
                rightStrings to rightColors
            }

        // Массив TextView и gravity для позиционирования текста
        val (children, childGravity) =
            if (pos == positionLeft) {
                leftViews to (Gravity.CENTER_VERTICAL or Gravity.END)
            } else {
                rightViews to (Gravity.CENTER_VERTICAL or Gravity.START)
            }

        frameLayout {
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)

            tag = pos

            for ((i, colorRes) in colors.withIndex()) {
                textView {
                    frameLayoutParams {
                        width = MATCH_PARENT
                        height = MATCH_PARENT
                        gravity = Gravity.CENTER // layout_gravity для TextView внутри FrameLayout
                    }

                    tag = pos + i
                    backgroundColor = colorRes
                    text = strings[i]
                    gravity = childGravity
                    textColor = Color.WHITE
                    padRight = dp_i(20f)
                    padLeft = dp_i(20f)
                    children += this
                }
            }
        }
    }

    /**
     * Основной элемент позиционируется по центру, занимая всю высоту и ширину.
     * Боковые элементы (FrameLayout) позиционируются за пределами границ основного элемента.
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val dbgPrefix =
            "${this::class.java.simpleName}::${object {}.javaClass.enclosingMethod?.name}"

        for (i in 0 until childCount) {

            val child = getChildAt(i)

            when {
                (child.tag == positionLeft) -> {
                    child.layout(
                        -child.measuredWidth,
                        0,
                        0, child.measuredHeight
                    )
                }
                (child.tag == positionRight) -> {
                    child.layout(
                        child.measuredWidth,
                        0,
                        child.measuredWidth * 2,
                        child.measuredHeight
                    )
                }
                else -> {
                    child.layout(0, 0, child.measuredWidth, child.measuredHeight)
                }
            }
        }
    }

    /**
     * При первом касании проверяем область тача и фиксируем координаты
     */
    private fun onTouchBegin(view: View, event: MotionEvent): Boolean {

        touchSlop = ViewConfiguration.get(view.context).scaledTouchSlop

        /**
         * В момент touch нужно зафиксировать положение левой границы view (upperLayer'а)
         * Это потребуется как база при анимировании из положения waiting в левую сторону.
         */
        viewGlobalX = view.x.toInt()


        rawTouchDownX = event.rawX
        rawTouchDownY = event.rawY

        return true
    }

    /**
     * https://stackoverflow.com/questions/33701657/setx-settranslationx-sety-and-settranslationy/33701994
     *
     * animate().x - это анимация значения свойста view.x, которое определяет абсолютное смещение
     * относительно родительского  top/left.
     *
     * animate().translationX - это анимация смещения от позиция нашего view, которое оно
     * получило при layout.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val dbgPrefix =
            "${this::class.java.simpleName}::${object {}.javaClass.enclosingMethod?.name}"

        return when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                itemView.clearAnimation()
                onTouchBegin(itemView, event)
                true
            }
            /**
             * Когда view зафиксирована (стоит) в положении waiting, то viewGlobalX фактически
             * равен view.translateX, view.x. Будем использовать это значение как якорь в событиях
             * когда мувим view из состояния waiting. В этом состоянии нужно менять x относительно
             * некой базы, которой и является viewGlobalX.
             */
            MotionEvent.ACTION_MOVE -> {

                val shiftX = abs(event.rawX - rawTouchDownX)
                val shiftY = abs(event.rawY - rawTouchDownY)

                if (shiftX >= touchSlop || shiftY >= touchSlop) {
                    // Полная дистанция от места тача до текущего положения пальца
                    val totalOffset = event.rawX - rawTouchDownX

                    val eventOffset = when (currentState) {
                        State.Start -> {
                            totalOffset
                        }
                        // Дистанция между текущим и предыдущим событием ACTION_MOVE
                        State.Waiting -> {
                            viewGlobalX + totalOffset
                        }
                    }

                    itemView.animate()
                        .x(eventOffset)
                        .setDuration(0)
                        .start()
                    animateSideViewsMoving(leftViews, eventOffset)
                    animateSideViewsMoving(rightViews, eventOffset)
                }
                true
            }
            MotionEvent.ACTION_UP -> {

                // Условия возврата в исходное положение зависят от текущего состояния
                val threshold = when (currentState) {
                    State.Start -> {
                        itemView.measuredWidth / 10
                    }
                    State.Waiting -> {
                        itemView.measuredWidth / 2
                    }
                }

                when {
                    (itemView.x != 0f && abs(itemView.x) >= threshold) -> {

                        // offset зависит от количества элементов в боковом контейнере, который
                        // становится видимым в результате перехода в состояние waiting.
                        val offset = sign(itemView.x) *
                                (itemView.measuredWidth.toFloat() - itemView.measuredWidth / (leftViews.size))

                        animator(itemView, offset, duration, animateToWaitingPosition).start()
                        animateSideViewsJump(leftViews, offset)
                        animateSideViewsJump(rightViews, offset)
                    }
                    (itemView.x != 0f && abs(itemView.x) < threshold) -> {

                        animator(itemView, 0f, duration, animateToStartPosition).start()
                        leftViews.forEach {
                            animator(
                                it,
                                0f,
                                duration,
                                animateToStartPosition
                            ).start()
                        }
                        rightViews.forEach {
                            animator(
                                it,
                                0f,
                                duration,
                                animateToStartPosition
                            ).start()
                        }
                    }
                    else -> {
                        performClick()
                    }
                }
                true
            }
            else -> {
                super.onTouchEvent(event)
            }
        }
    }

    /**
     * При анимировании боковых View нужно учитывать их позицию в массиве. Боковые view расположены
     * внутри FrameLayout друг над другом. Последний элемент массива - самый верхний. При
     * анимировании элементы должны выезжать за основным view с разной "скоростью". Скорость зависит
     * от количества элементов в массиве и регулируется коэффициентом k. Самый последний в массиве
     * (он же самый верхний) выезжает медленнее всех.
     */
    private fun animateSideViewsMoving(views: List<View>, offset: Float) {

        for ((i, view) in views.withIndex()) {

            val k: Float = 1f - i.toFloat() / views.size

            view.animate()
                .x(offset * k)
                .setDuration(0)
                .start()
        }
    }

    private fun animateSideViewsJump(views: List<View>, offset: Float) {
        for ((i, view) in views.withIndex()) {

            val k: Float = 1f - i.toFloat() / views.size
            animator(view, offset * k, duration, animateToWaitingPosition).start()
        }
    }

    override fun performClick(): Boolean {
        val dbgPrefix =
            "${this::class.java.simpleName}::${object {}.javaClass.enclosingMethod?.name}"
        logIt("$dbgPrefix", TAG_SWIPE)

        return super.performClick()
    }

    override fun onClick(v: View?) {
        val dbgPrefix =
            "${this::class.java.simpleName}::${object {}.javaClass.enclosingMethod?.name}"

        logIt("$dbgPrefix", TAG_SWIPE)
    }

    /**
     * Сравнить длины массивов. Длины массивов строк и цветов для боковой панели должны совпадать.
     */
    private fun compareArrays(arr1: ArrayList<*>?, arr2: ArrayList<*>?) {
        if (arr1 != null && arr2 != null) {
            check(arr1.size == arr2.size) { "String and Color arrays must be the same size" }
        }
    }
}