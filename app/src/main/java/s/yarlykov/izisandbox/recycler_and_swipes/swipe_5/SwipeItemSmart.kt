package s.yarlykov.izisandbox.recycler_and_swipes.swipe_5

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
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

private const val TAG_SWIPE = "TAG_SWIPE"

const val noId = 0
const val positionLeft = 1000
const val positionRight = 1001

class SwipeItemSmart : FrameLayout, View.OnClickListener {

    private lateinit var cardView: View

    private var itemLayoutId: Int = noId
    private var leftFrame: Boolean = false
    private var rightFrame: Boolean = false
    private var rightColorsRes: Int = noId
    private var leftColorsRes: Int = noId
    private var leftTextRes: Int = noId
    private var rightTextRes: Int = noId

    private var leftStrings = arrayListOf<String>()
    private var rightStrings = arrayListOf<String>()
    private var leftColors = arrayListOf<Int>()
    private var rightColors = arrayListOf<Int>()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setUpAttrs(attrs)
        setupItemView()
    }

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

    private fun setupItemView() {
        if (itemLayoutId != noId) {
            cardView = LayoutInflater.from(context).inflate(itemLayoutId, null)

            compareArrays(leftStrings, leftColors)
            compareArrays(rightStrings, rightColors)

            addView(cardView)

            createSideViews(positionLeft)
            createSideViews(positionRight)

            cardView.bringToFront()
        }
    }

    private fun compareArrays(arr1: ArrayList<*>?, arr2: ArrayList<*>?) {
        if (arr1 != null && arr2 != null) {
            check(arr1.size != arr2.size) { "String and Color arrays must be the same size" }
        }
    }

    private fun createSideViews(pos: Int) {

        val strings = if (pos == positionLeft) leftStrings else rightStrings
        val colors = if (pos == positionLeft) leftColors else rightColors

        val fl = frameLayout {
            layoutParams = ViewGroup.LayoutParams(
                MATCH_PARENT,
                MATCH_PARENT
            )

            tag = pos

            for ((i, colorRes) in colors.withIndex()) {

                textView {
                    frameLayoutParams {
                        width = MATCH_PARENT
                        height = MATCH_PARENT
                        gravity = Gravity.CENTER
                    }

                    tag = i

                    backgroundColor = colorRes
                    text = strings[i]
                    gravity = Gravity.CENTER
                    textColor = Color.WHITE
                    padRight = dp_i(20f)
                    padLeft = dp_i(20f)
                }
            }
        }

        addView(fl)
    }

    enum class State {
        Start,
        Waiting
    }

    private var dX = 0f
    private var rawTouchDownX = 0f
    private var rawTouchDownY = 0f
    private val duration = 100L

    private var viewGlobalX = 0
    private var touchSlop = 0

    private var currentPosition = State.Start

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

    private val animateToStartPosition = AnimatorListenerTemplate(
        onStart = {
        },
        onEnd = {
            currentPosition = State.Start
        })

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
                    logIt(
                        "$dbgPrefix ${child::class.java.simpleName}, from else: child.measuredWidth=${child.measuredWidth}",
                        TAG_SWIPE
                    )
                    child.layout(0, 0, child.measuredWidth, child.measuredHeight)
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

    }

    /**
     * При первом касании проверяем область тача и фиксируем координаты
     */
    private fun onTouchBegin(view: View, event: MotionEvent): Boolean {

        touchSlop = ViewConfiguration.get(view.context).scaledTouchSlop

        val rect = Rect()
        view.getGlobalVisibleRect(rect)
        viewGlobalX = rect.left

        rawTouchDownX = event.rawX
        rawTouchDownY = event.rawY

        dX = viewGlobalX - event.rawX
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

        logIt("$dbgPrefix", TAG_SWIPE)

        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                onTouchBegin(cardView, event)
                true
            }
            MotionEvent.ACTION_MOVE -> {

                val shiftX = abs(event.rawX - rawTouchDownX)
                val shiftY = abs(event.rawY - rawTouchDownY)

                if (shiftX >= touchSlop || shiftY >= touchSlop) {
                    cardView.animate()
                        .translationX(event.rawX - rawTouchDownX)
                        .setDuration(0)
                        .start()
                    tvBlue.animate()
                        .translationX(event.rawX - rawTouchDownX)
                        .setDuration(0)
                        .start()
                    tvGreen.animate()
                        .translationX((event.rawX - rawTouchDownX) * 0.6f)
                        .setDuration(0)
                        .start()
                    tvRed.animate()
                        .translationX((event.rawX - rawTouchDownX) * 0.3f)
                        .setDuration(0)
                        .start()
                    tvGray.animate()
                        .translationX(event.rawX - rawTouchDownX)
                        .setDuration(0)
                        .start()
                    tvPurple.animate()
                        .translationX((event.rawX - rawTouchDownX) * 0.5f)
                        .setDuration(0)
                        .start()
                }
                true
            }
            MotionEvent.ACTION_UP -> {
                if (x != 0f) {
                    animator(cardView, 0f, duration, animateToStartPosition).start()
                    animator(tvBlue, 0f, duration, animateToStartPosition).start()
                    animator(tvGreen, 0f, duration, animateToStartPosition).start()
                    animator(tvRed, 0f, duration, animateToStartPosition).start()
                    animator(tvPurple, 0f, duration, animateToStartPosition).start()
                    animator(tvGray, 0f, duration, animateToStartPosition).start()
                } else {
                    performClick()
                }

                true
            }
            else -> {
                super.onTouchEvent(event)
            }
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
}