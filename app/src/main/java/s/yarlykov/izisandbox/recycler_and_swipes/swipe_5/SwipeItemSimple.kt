package s.yarlykov.izisandbox.recycler_and_swipes.swipe_5

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.Utils.logIt
import kotlin.math.abs

private const val TAG_SWIPE = "TAG_SWIPE"

/**
 * Я пробовал размещать ещё одну FrameLayout в качестве контейнера для TextView справа.
 * Идея была такова: во время layout'а сдвигать эту FrameLayout за правый край. Эта FrameLayout
 * сама расположит свои дочерние TextView внутри себя. Во время MotionEvent.ACTION_MOVE я хотел
 * перемещать её дочерние TextView следом за карточкой, но проблема в том, что эти TextView
 * отрисовываются только в границах своего родителя. Все что выходит за пределы - обрезается !
 *
 * Однако !!!! Это можно поправить с помощью атрибута android:clipChildren="false"
 * Я поставил его в элемент SwipeItem и все завелось !!!
 *
 */
class SwipeItemSimple @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), View.OnClickListener {

    enum class Position {
        Start,
        Waiting
    }

    private var dX = 0f
    private var rawTouchDownX = 0f
    private var rawTouchDownY = 0f
    private val duration = 100L

    private var viewGlobalX = 0
    private var touchSlop = 0

    private var currentPosition = Position.Start

    private val leftViews = listOf(R.id.tv_blue, R.id.tv_green, R.id.tv_red)
    private val rightViews = listOf(R.id.tv_gray, R.id.tv_purple)

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
            currentPosition = Position.Start
        })

    private lateinit var cardView: MaterialCardView
    private lateinit var tvBlue: TextView
    private lateinit var tvGreen: TextView
    private lateinit var tvRed: TextView
    private lateinit var tvGray: TextView
    private lateinit var tvPurple: TextView

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val dbgPrefix =
            "${this::class.java.simpleName}::${object {}.javaClass.enclosingMethod?.name}"

        for (i in 0 until childCount) {

            when(val child = getChildAt(i)) {
                is TextView -> {
                    if(child.id in leftViews)
                    child.layout(-child.measuredWidth, 0, 0, child.measuredHeight)
                }
                !is MaterialCardView -> {
                    child.layout(child.measuredWidth, 0, child.measuredWidth * 2, child.measuredHeight)
                }
                else -> {
                    child.layout(0, 0, child.measuredWidth, child.measuredHeight)
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        cardView = findViewById(R.id.card_view)
        tvBlue = findViewById(R.id.tv_blue)
        tvGreen = findViewById(R.id.tv_green)
        tvRed = findViewById(R.id.tv_red)
        tvGray = findViewById(R.id.tv_gray)
        tvPurple = findViewById(R.id.tv_purple)
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
                if (cardView.x != 0f) {
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