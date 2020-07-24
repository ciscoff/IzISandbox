package s.yarlykov.izisandbox.recycler_and_swipes.swipe_3

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.cardview.widget.CardView
import s.yarlykov.izisandbox.R

object Animators {

    private const val defaultDuration = 200L
    private val defaultInterpolator = LinearInterpolator()

    fun hide(view: View?, before: (() -> Unit)? = null, after: (() -> Unit)? = null) {

        view?.let { _view ->
            ObjectAnimator.ofFloat(_view, View.ALPHA, 0f).apply {
                interpolator = LinearInterpolator()
                duration = defaultDuration
                addListener(listener(before, after))
            }.start()
        }
    }

    fun scale(
        view: View?,
        factor: Float,
        before: (() -> Unit)? = null,
        after: (() -> Unit)? = null
    ) {
        view?.let { _view ->

            val scaleX = ObjectAnimator.ofFloat(_view, View.SCALE_X, factor).apply {
                interpolator = LinearInterpolator()
                duration = defaultDuration
            }
            val scaleY = ObjectAnimator.ofFloat(_view, View.SCALE_Y, factor).apply {
                interpolator = LinearInterpolator()
                duration = defaultDuration
            }

            AnimatorSet().apply {
                addListener(listener(before, after))
                playTogether(scaleX, scaleY)
            }.start()
        }
    }

    /**
     * Анимирует цвет, то есть работает со значениями Int, которые интерпретирует как ARGB
     *
     */
    fun color(
        view: View?,
        colorFrom: Int,
        colorTo: Int,
        before: (() -> Unit)? = null,
        after: (() -> Unit)? = null
    ) {
        ObjectAnimator.ofObject(
            view,
            "backgroundColor",
            ArgbEvaluator(),
            colorFrom,
            colorTo
        ).apply {
            interpolator = defaultInterpolator
            duration = defaultDuration
            addListener(listener(before, after))
        }.start()
    }

    fun cardColor(
        view: CardView?,
        colorFrom: Int,
        colorTo: Int,
        before: (() -> Unit)? = null,
        after: (() -> Unit)? = null
    ) {
        val colors = arrayOf(ColorDrawable(colorFrom), ColorDrawable(colorTo))
        val trans = TransitionDrawable(colors)

        view?.background = trans
        trans.startTransition(defaultDuration.toInt())
    }

    fun translateX(
        view: View?,
        shift: Float,
        animDuration: Long = defaultDuration,
        before: (() -> Unit)? = null,
        after: (() -> Unit)? = null
    ) {

        view?.let { _view ->
            ObjectAnimator.ofFloat(_view, View.TRANSLATION_X, shift).apply {
                interpolator = defaultInterpolator
                duration = animDuration
                addListener(listener(before, after))
            }.start()
        }
    }

    /**
     * Генерит AnimatorListenerTemplate с указанным набором кода
     */
    private fun listener(before: (() -> Unit)?, after: (() -> Unit)?): Animator.AnimatorListener {
        return AnimatorListenerTemplate(
            onStart = {
                before?.invoke()
            },
            onEnd = {
                after?.invoke()
            })
    }
}