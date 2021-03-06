package s.yarlykov.izisandbox.recycler_and_swipes.swipe_5

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.util.AttributeSet
import android.view.*
import android.view.View.OnTouchListener
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.dsl.extenstions.*
import s.yarlykov.izisandbox.dsl.frameLayout
import s.yarlykov.izisandbox.dsl.frameLayoutParams
import s.yarlykov.izisandbox.dsl.textView
import s.yarlykov.izisandbox.extensions.findMostSuitable
import s.yarlykov.izisandbox.extensions.showSnackBarNotification
import s.yarlykov.izisandbox.utils.logIt
import kotlin.math.abs
import kotlin.math.sign

private const val TAG_SWIPE = "TAG_SWIPE"

private const val noId = 0
private const val noState = -1
private const val sideLeft = 1000
private const val sideRight = 2000

class SwipeItemSmartV2 : FrameLayout, View.OnTouchListener, View.OnClickListener {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setupAttrs(attrs)
        setupNestedViews()
    }

    enum class State {
        Start,
        Waiting,
        Animating
    }

    /**
     * View переднего плана (CardView)
     */
    private lateinit var frontView: View
    private val frontElevation = 2f

    /**
     * Набор переменных, инициализируемых через кастомные атрибуты в XML
     */
    private var leftFrame: Boolean = false
    private var rightFrame: Boolean = false
    private var itemLayoutId: Int = noId

    private var stateId: Int = noState

    private lateinit var leftStrings: List<String>
    private lateinit var rightStrings: List<String>
    private var leftColors = emptyList<Int>()
    private var rightColors = emptyList<Int>()
    private var leftTags = emptyList<Int>()
    private var rightTags = emptyList<Int>()

    private var leftViews = mutableListOf<View>()
    private var rightViews = mutableListOf<View>()
    private val sideViews: List<View>
        get() = leftViews + rightViews

    /**
     * Переменные для работы со свайпом
     */
    private var rawTouchDownX = 0f
    private var rawTouchDownY = 0f
    private val duration = 400L

    private var viewGlobalX = 0
    private var touchSlop = 0

    private val clickFaultDistance = 8
    private var stateCurrent = State.Start

    private var stateNext: State = State.Waiting

    // Условие перехода в состояние Waiting
    private val thresholdRatio = 0.1f

    private val thresholdPullRatio = 0.9f

    // В состоянии Start frontView перешла порог, после которого должна автоматически
    //  анимироваться в состояния Waiting
    private val Float.isOverThreshold: Boolean
        get() = abs(this - rawTouchDownX) > frontView.measuredWidth * thresholdRatio

    // В состоянии Start frontView пытяются растянуть дальше её границы за пределы видимости
    private val Float.isOverScrolled: Boolean
        get() = abs(this) >= frontView.measuredWidth * thresholdPullRatio

    // В состоянии Waiting frontView пытаются толкнуть дальше её границы за пределы видимости
    private val Float.isOverPulled: Boolean
        get() = sign(this * frontView.x) > 0

    private val maxOffset: Float
        get() = frontView.measuredWidth * 0.9f

    init {
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    object animateX {

        private const val tension = 0.7f

        operator fun invoke(
            view: View,
            shift: Float,
            animationDuration: Long,
            listener: Animator.AnimatorListener
        ) = ObjectAnimator.ofFloat(view, "x", shift).apply {
            interpolator = OvershootInterpolator(tension)
            duration = animationDuration
            addListener(listener)
        }.start()
    }

    private val animateToStartPosition = AnimatorListenerTemplate(
        onStart = {
            // Чтобы не реагировать на касания во время анимации.
            frontView.isEnabled = false
            stateCurrent = State.Animating
        },
        onEnd = {
            stateCurrent = State.Start
            stateNext = State.Waiting
            frontView.isEnabled = true
        })

    private val animateToWaitingPosition = AnimatorListenerTemplate(
        onStart = {
            // Чтобы не реагировать на касания во время анимации.
            frontView.isEnabled = false
            stateCurrent = State.Animating
        },
        onEnd = {
            stateCurrent = State.Waiting
            stateNext = State.Start
            frontView.isEnabled = true
        })

    /**
     * Прочитать атрибуты, инициализировать переменные класса
     */
    private fun setupAttrs(attrs: AttributeSet?) {
        context.obtainStyledAttributes(attrs, R.styleable.SwipeItemSmartV2).apply {
            stateId = getInt(R.styleable.SwipeItemSmartV2_state, noState)
            itemLayoutId = getResourceId(R.styleable.SwipeItemSmartV2_layoutId, noId)
            leftFrame = getBoolean(R.styleable.SwipeItemSmartV2_leftFrame, false)
            rightFrame = getBoolean(R.styleable.SwipeItemSmartV2_rightFrame, false)
        }.recycle()

        ItemDescriptor.state(stateId)?.let { s ->
            leftStrings = s.leftStrings.map { context.getString(it) }
            rightStrings = s.rightStrings.map { context.getString(it) }

            leftColors = s.leftColors.map { ContextCompat.getColor(context, it) }
            rightColors = s.rightColors.map { ContextCompat.getColor(context, it) }

            leftTags = s.leftTags
            rightTags = s.rightTags

        } ?: throw IllegalStateException("Invalid state value")
    }

    /**
     * Добавить View основного элемента. Добавить Views боковых элементов.
     */
    private fun setupNestedViews() {
        compareArrays(leftStrings, leftColors)
        compareArrays(leftTags, leftColors)
        compareArrays(rightStrings, rightColors)
        compareArrays(rightTags, rightColors)

        if (itemLayoutId != noId) {
            frontView = LayoutInflater.from(context).inflate(itemLayoutId, null).apply {
                setOnTouchListener(this@SwipeItemSmartV2)
                setOnClickListener(this@SwipeItemSmartV2)
                elevation = dp_f(frontElevation) // ??
            }

            if (leftFrame) addSideViews(sideLeft)
            if (rightFrame) addSideViews(sideRight)

            initChildren(OnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.showSnackBarNotification("Clicked item ${(v as TextView).text}")
                        false
                    }
                    MotionEvent.ACTION_UP -> {
                        v.performClick()
                        false
                    }
                    else -> {
                        false
                    }
                }
            })

            addView(frontView)
            frontView.bringToFront()
        }
    }

    /**
     * Добавить боковые элементы (это контейнеры FrameLayout с вложенными TextView)
     */
    private fun addSideViews(sideId: Int) {

        // Цвет фона и текст для каждой TextView
        val (strings, colors) =
            if (sideId == sideLeft) {
                leftStrings to leftColors
            } else {
                rightStrings to rightColors
            }

        // Массив TextView и gravity для позиционирования текста
        val (children, childGravity) =
            if (sideId == sideLeft) {
                leftViews to (Gravity.CENTER_VERTICAL or Gravity.END)
            } else {
                rightViews to (Gravity.CENTER_VERTICAL or Gravity.START)
            }

        val tags = if (sideId == sideLeft) leftTags else rightTags

        frameLayout {
            layoutParams = LayoutParams(
                MATCH_PARENT,
                MATCH_PARENT
            )

            tag = sideId

            for ((i, colorRes) in colors.withIndex()) {
                textView {
                    frameLayoutParams {
                        width = MATCH_PARENT
                        height = MATCH_PARENT
                        gravity = Gravity.CENTER // layout_gravity для TextView внутри FrameLayout
                    }

                    tag = tags[i]
                    text = strings[i]
                    gravity = childGravity
                    backgroundColor = colorRes
                    textColor = Color.WHITE
                    padRight = dp_i(20f)
                    padLeft = dp_i(20f)
                    isClickable = true
                    isFocusable = true
                    children += this
                }
            }

//            view {
//                frameLayoutParams {
//                    width = ViewGroup.LayoutParams.MATCH_PARENT
//                    height = ViewGroup.LayoutParams.MATCH_PARENT
//                }
//                backgroundDrawable = R.drawable.background_rounded_transparent_wide
//            }
        }
    }

    private fun initChildren(listener: OnTouchListener) {
        frontView.x = 0f
        sideViews.forEach {
            it.setOnTouchListener(listener)
            it.x = 0f
        }
    }

    /**
     * Основной элемент позиционируется по центру, занимая всю высоту и ширину.
     * Боковые элементы (FrameLayout) позиционируются за пределами границ основного элемента.
     *
     * NOTE: Layout для детей боковых элементов (цветных Views) инициируют их родительские
     * FrameLayout.
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {

        for (i in 0 until childCount) {

            val child = getChildAt(i)

            when {
                (child.tag == sideLeft) -> {
                    child.layout(
                        -child.measuredWidth,
                        0,
                        0,
                        child.measuredHeight
                    )
                }
                (child.tag == sideRight) -> {
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
     * Нужно помнить, что если view вернет true на ACTION_DOWN, то система начинает генерить и
     * отправлять этой view событие ACTION_MOVE даже если палец стоит на месте. Поэтому в диспетчере
     * это нужно учитывать. Ниже как раз такой случай.
     *
     * Важный момент относительно ACTION_DOWN/ACTION_UP в состоянии State.Waiting.
     * Если DOWN произошел на цветной карточке, то её dispatchTouchEvent вернет true. И если
     * сделать return colorView.dispatchTouchEvent(event), то это означает, что наш
     * dispatchTouchEvent теперь будет получать от системы поток сообщений ACTION_MOVE и при
     * неправильной маршрутизации весь этот поток полетит в frontView (см код ниже). Чтобы не было
     * косяков и кривого поведения frontView поступаем следующим образом: если ACTION_DOWN направляем
     * в цветную view, то возвращаем false, а если направляем в frontView, то возвращаем true.
     */
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {

        return when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> {
                when (stateCurrent) {
                    // В этом состоянии frontView всегда получает UP/DOWN
                    State.Start -> {
                        frontView.dispatchTouchEvent(event)
                    }
                    // В этом состоянии UP/DOWN получает View, которая под тачем (это может быть
                    // одна из цветных View или frontView).
                    State.Waiting -> {
                        (sideViews + listOf(frontView))
                            .map {
                                val rect = Rect()
                                it.getGlobalVisibleRect(rect)
                                rect to it
                            }.toMap()
                            .findMostSuitable(event.rawX, event.rawY)?.let { eventOwner ->
                                eventOwner.dispatchTouchEvent(event)
                                eventOwner::class == frontView::class // см коммент выше
                            } ?: false
                    }
                    // При анимировании никто не получает UP/DOWN (это промежуточное состояние,
                    // на тачи не раегируем)
                    State.Animating -> {
                        false
                    }
                }
            }
            // MOVE можно получить, только если до этого был получен ACTION_DOWN, а это
            MotionEvent.ACTION_MOVE -> {
                if (stateCurrent == State.Start || stateCurrent == State.Waiting) {
                    frontView.dispatchTouchEvent(event)
                } else {
                    false
                }
            }
            else -> {
                super.dispatchTouchEvent(event)
            }
        }
    }

    /**
     * При первом касании проверяем область тача и фиксируем координаты
     */
    private fun onTouchBegin(view: View, event: MotionEvent): Boolean {

        val rect = Rect()
        view.getGlobalVisibleRect(rect)

        return if (stateCurrent != State.Animating && rect.contains(
                event.rawX.toInt(),
                event.rawY.toInt()
            )
        ) {

            /**
             * В момент touch нужно зафиксировать положение левой границы view (upperLayer'а)
             * Это потребуется как база при анимировании из положения waiting в левую сторону.
             */
            viewGlobalX = view.x.toInt()

            rawTouchDownX = event.rawX
            rawTouchDownY = event.rawY
            true
        } else {
            false
        }
    }

    /**
     * Реализация onTouchListener для frontView (CardView)
     *
     * https://stackoverflow.com/questions/33701657/setx-settranslationx-sety-and-settranslationy/33701994
     *
     * animate().x - это анимация значения свойста view.x, которое определяет абсолютное смещение
     * относительно родительского  top/left.
     *
     * animate().translationX - это анимация смещения от позиция нашего view, которое оно
     * получило при layout.
     */
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        val dbgPrefix =
            "${this::class.java.simpleName}::${object {}.javaClass.enclosingMethod?.name}"

        return when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                return onTouchBegin(view, event)
            }
            MotionEvent.ACTION_MOVE -> {
                // Не начинать обработку, если отсутствует соотв боковой контейнер
                if (stateCurrent == State.Start) {
                    if (event.rawX - rawTouchDownX > 0 && !leftFrame) return false
                    if (event.rawX - rawTouchDownX < 0 && !rightFrame) return false
                }

                // Определить следующее состояние, если текущее State.Start
                calculateStateTo(event, view.x)

                val shiftX = abs(event.rawX - rawTouchDownX)
                val shiftY = abs(event.rawY - rawTouchDownY)

//                if (shiftX >= touchSlop || shiftY >= touchSlop) {
//
                if (shiftY == 0f || shiftX > shiftY / 5) {
                    parent.requestDisallowInterceptTouchEvent(true)

                    // Полная дистанция от места тача до текущего положения пальца
                    val rawOffset = event.rawX - rawTouchDownX

                    val eventOffset = when (stateCurrent) {
                        State.Start -> {
                            if (rawOffset.isOverScrolled) maxOffset * sign(view.x) else rawOffset
                        }
                        // Дистанция между текущим и предыдущим событием ACTION_MOVE (viewGlobalX + rawOffset)
                        State.Waiting -> {
                            if (rawOffset.isOverPulled) 0f else viewGlobalX + rawOffset
                        }
                        State.Animating -> {
                            0f
                        }
                    }

                    if (eventOffset != 0f) {
                        view.animate().x(eventOffset).setDuration(0).start()
                        animateSideViewsMoving(leftViews, eventOffset)
                        animateSideViewsMoving(rightViews, eventOffset)
                    }
                }
//                }
                true
            }
            MotionEvent.ACTION_UP -> {
                parent.requestDisallowInterceptTouchEvent(false)

                when {
                    (stateCurrent == State.Start && view.x == 0f) -> {
                        view.performClick()
                    }
                    (stateCurrent == State.Start) -> {

                        if (stateNext == State.Start) {
                            forceToStartPosition()
                        } else {
                            // Только один элемент в родительском RecyclerView может быть с открытым боковым фреймом.
                            forceSiblingsToStartPosition()

                            // Адаптивно меняем размер открываемого пространства: чем меньше
                            // элементов в открываемом массиве view, тем меньше смещаем upperLayer.
                            // (Надо доработать !!!)
                            val sign = sign(view.x)
                            val sideItemsQty = if (sign > 0) leftViews.size else rightViews.size
                            val k: Float = 1f - 1f / (sideItemsQty * 2)
                            val offset = sign * (view.measuredWidth * k)

                            animateX(view, offset, duration, animateToWaitingPosition)
                            animateSideViewsJump(leftViews, offset)
                            animateSideViewsJump(rightViews, offset)
                        }
                    }
                    // Если в состоянии Waiting требуется реагировать на клик, то игнорим мелкие
                    // погрешности движения пальца.
                    // (????) Вообщето в таком случае лучше никак не реагировать или вернуться в Start
                    (stateCurrent == State.Waiting && abs(event.rawX - rawTouchDownX) < clickFaultDistance) -> {
                        view.performClick()
                    }
                    // Однако, если мы в состоянии Waiting и дистанция больше clickFaultDistance,
                    // то возвращаемся в состояние Start.
                    (stateCurrent == State.Waiting && view.x != 0f) -> {
                        forceToStartPosition()
                    }
                    else -> {
                        view.performClick()
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
     * В состоянии State.Start пользователь может водить слайдер вправо и влево не отпуская
     * пальца. Нужно знать направление движения в момент непосредственно перед ACTION_UP.
     * Например перед ACTION_UP пользователь повел палец обратно к исходной позиции,
     * значит и вернуться нужно в исходную, а не в Waiting.
     *
     * NOTE: У event есть различные значения X. Например e.getRawX() отличается от e.getX() тем,
     * что первое показывает координату относительно экрана девайса, а второе - координата
     * относительно left/top той view, которая приняла эвент. Исторические данные передаются
     * именно как getX, поэтому и сравнивать исторический массив нужно с текущим getX.
     *
     * Условия смены состояния зависят от знака дельты координат пальца и знака смещения view слайдера.
     *
     * dX > 0, view.x > 0: to Waiting
     * dX < 0, view.x > 0: to Start
     * dX < 0, view.x < 0: to Waiting
     * dX > 0, view.x < 0: to Start
     *
     */
    private fun calculateStateTo(event: MotionEvent, viewX: Float) {
        val historySize = event.historySize

        if (historySize > 0) {
            val dX = event.x - event.getHistoricalX(0, historySize - 1)

            val toWaiting = sign(dX * viewX) > 0

            if (stateCurrent == State.Start) {
                stateNext =
                    if (event.rawX.isOverThreshold) {
                        if (toWaiting) State.Waiting else State.Start
                    } else {
                        State.Start
                    }
            }
        }
    }

    /**
     * Вернуть все дочерние элементы в исходное положение
     */
    fun forceToStartPosition() {
        animateX(frontView, 0f, duration, animateToStartPosition)
        sideViews.forEach { animateX(it, 0f, duration, animateToStartPosition) }
    }

    /**
     * Вернуть всех братьев внутри родительского RecyclerView в состояние start.
     */
    private fun forceSiblingsToStartPosition() {
        // TODO nothing
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
            view.animate().x(offset * k).setDuration(0).start()
        }
    }

    private fun animateSideViewsJump(views: List<View>, offset: Float) {
        for ((i, view) in views.withIndex()) {
            val k: Float = 1f - i.toFloat() / views.size
            animateX(view, offset * k, duration, animateToWaitingPosition)
        }
    }

    override fun onClick(v: View) {
        val dbgPrefix =
            "${this::class.java.simpleName}::${object {}.javaClass.enclosingMethod?.name}"
    }

    /**
     * Сравнение длин массивов.
     */
    private fun compareArrays(arr1: List<*>?, arr2: List<*>?) {
        if (arr1 != null && arr2 != null) {
            check(arr1.size == arr2.size) { "String and Color arrays must be the same size" }
        }
    }

    /**
     * DEBUG
     */
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        val dbgPrefix =
            "${this::class.java.simpleName}::${object {}.javaClass.enclosingMethod?.name}"

        return when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                false
            }
            MotionEvent.ACTION_MOVE -> {
                false
            }
            MotionEvent.ACTION_UP -> {
                false
            }
            else -> {
                super.onInterceptTouchEvent(event)
            }
        }
    }

    /**
     * DEBUG
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val dbgPrefix =
            "${this::class.java.simpleName}::${object {}.javaClass.enclosingMethod?.name}"
        when (event.action) {
            MotionEvent.ACTION_DOWN -> logIt("$dbgPrefix ACTION_DOWN", false, TAG_SWIPE)
            MotionEvent.ACTION_MOVE -> logIt("$dbgPrefix ACTION_MOVE", false, TAG_SWIPE)
            MotionEvent.ACTION_UP -> logIt("$dbgPrefix ACTION_UP", false, TAG_SWIPE)
        }

        return super.onTouchEvent(event)
    }
}