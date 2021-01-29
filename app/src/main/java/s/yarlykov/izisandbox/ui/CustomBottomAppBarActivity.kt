package s.yarlykov.izisandbox.ui

import android.animation.ValueAnimator
import android.animation.ValueAnimator.INFINITE
import android.animation.ValueAnimator.REVERSE
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.text.TextPaint
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomappbar.BottomAppBar
import kotlinx.android.synthetic.main.activity_custom_bottom_app_bar.*
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.utils.logIt
import kotlin.properties.Delegates

class CustomBottomAppBarActivity : AppCompatActivity(), View.OnClickListener {

    private var isBarVisible: Boolean by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity {
            layout = R.layout.activity_custom_bottom_app_bar

            barParent {
                clickListener = this@CustomBottomAppBarActivity

                initChildren {
                    bottomAppBar {
                        initState = hidden
                        initElevation = 0f
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        animateTextColors()
    }

    override fun onClick(v: View?) {
        if (isBarVisible) {
            bottomBar.performHide()
        } else {
            bottomBar.visibility = View.VISIBLE
            bottomBar.performShow()
        }
        isBarVisible = !isBarVisible
    }

    /**
     * ValueAnimator будет последовательно работать с парами из списка аргументов.
     * Например сначала он возьмет первые два массива (нулевой и первый) и прогонит через
     * эту пару весь диапазон значений fraction (от 0 до 1). Затем возьмет ещё два массива
     * (первый и второй) и снова прогонит fraction от 0 до 1 и так далее.
     */
    private fun animateTextColors() {

        val paint: TextPaint = textViewColored.paint
        val textWidth = paint.measureText(textViewColored.text.toString())

        ValueAnimator.ofObject(
            GradientArgbEvaluator(),
            intArrayOf(Color.RED, Color.RED, Color.RED),
            intArrayOf(Color.RED, Color.RED, Color.GREEN),
            intArrayOf(Color.RED, Color.GREEN, Color.BLACK),
            intArrayOf(Color.GREEN, Color.BLACK, Color.MAGENTA),
            intArrayOf(Color.BLACK, Color.MAGENTA, Color.BLUE),
            intArrayOf(Color.MAGENTA, Color.BLUE, Color.BLUE),
            intArrayOf(Color.BLUE, Color.BLUE, Color.BLUE)
        ).apply {
            repeatMode = REVERSE
            repeatCount = INFINITE
            duration = 5000

            interpolator = AccelerateDecelerateInterpolator()

            addUpdateListener { animator ->

                val textShader: Shader = LinearGradient(
                    0f, 0f, textWidth, textViewColored.textSize,
                    animator.animatedValue as IntArray, null,
                    Shader.TileMode.CLAMP
                )
                textViewColored.paint.shader = textShader
                textViewColored.invalidate()
            }
        }.start()
    }

    /**
     * DSL Block
     */
    private fun activity(op: AppCompatActivity.() -> Unit) = apply(op)

    private var AppCompatActivity.layout: Int
        get() = 0
        set(value) {
            setContentView(value)
        }

    private fun barParent(op: View.() -> Unit) = barParent.op()

    private var View.clickListener: View.OnClickListener
        get() = View.OnClickListener {}
        set(value) {
            setOnClickListener(value)
        }

    private fun bottomAppBar(op: BottomAppBar.() -> Unit) = bottomBar.apply(op)

    private interface BarState
    private object hidden : BarState
    private object visible : BarState

    private fun initChildren(op: () -> Unit) = op()

    private var BottomAppBar.initState: BarState
        set(value) {
            when (value) {
                hidden -> {
                    isBarVisible = false
                    postDelayed({ performHide() }, 100)
                }
                visible -> {
                    isBarVisible = true
                    bottomBar.visibility = View.VISIBLE
                }
            }
        }
        get() = hidden

    private var BottomAppBar.initElevation: Float
        set(value) {
            elevation = value
        }
        get() = 0f
}