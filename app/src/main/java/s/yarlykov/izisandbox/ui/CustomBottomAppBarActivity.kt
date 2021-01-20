package s.yarlykov.izisandbox.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomappbar.BottomAppBar
import kotlinx.android.synthetic.main.activity_custom_bottom_app_bar.*
import s.yarlykov.izisandbox.R
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
                    postDelayed({performHide()}, 100)
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