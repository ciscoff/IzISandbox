package s.yarlykov.izisandbox.dsl

import android.os.Bundle
import android.view.Gravity.*
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.dsl.extenstions.*

class DslActivity : AppCompatActivity() {

    private lateinit var rootView: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dsl)

        rootView = findViewById(R.id.dsl_activity_root)

        addChildren2()
    }

    private fun addChildren1() {
        val ll = vc<LinearLayout>(this) {

            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            orientation = VERTICAL

            vp<TextView>(this) {

                linearLayoutParams {
                    width = dp_i(200f)
                    height = WRAP_CONTENT
                    gravity = END
                }

                background = fromDrawable(R.drawable.background_tv_rounded_red)
                text = from(R.string.tv_first)
                padTop = dp_i(10f)
                padBottom = dp_i(10f)
                gravity = CENTER
            }

            vp<TextView>(this) {

                linearLayoutParams {
                    width = dp_i(200f)
                    height = WRAP_CONTENT
                    gravity = START
                }
                background = fromDrawable(R.drawable.background_tv_rounded_yellow)
                text = from(R.string.tv_second)
                padTop = dp_i(10f)
                padBottom = dp_i(10f)
            }
        }

        rootView.addView(ll)
    }

    private fun addChildren2() {
        val ll = linearLayout {

            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            orientation = VERTICAL

            textView {

                linearLayoutParams {
                    width = dp_i(200f)
                    height = WRAP_CONTENT
                    gravity = END
                }

                background = fromDrawable(R.drawable.background_tv_rounded_red)
                text = from(R.string.tv_first)
                padTop = dp_i(10f)
                padBottom = dp_i(10f)
                gravity = CENTER
            }

            textView {

                linearLayoutParams {
                    width = dp_i(200f)
                    height = WRAP_CONTENT
                    gravity = START
                }

                background = fromDrawable(R.drawable.background_tv_rounded_yellow)
                text = from(R.string.tv_second)
                padTop = dp_i(10f)
                padBottom = dp_i(10f)
            }
        }
        rootView.addView(ll)
    }
}