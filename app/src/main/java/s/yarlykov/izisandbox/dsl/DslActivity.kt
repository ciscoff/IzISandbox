package s.yarlykov.izisandbox.dsl

import android.graphics.Color
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
import s.yarlykov.izisandbox.Utils.logIt
import s.yarlykov.izisandbox.dsl.extenstions.*

const val ID_HTML_VIEW = 100500

class DslActivity : AppCompatActivity() {

    private lateinit var rootView: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dsl)

        rootView = findViewById(R.id.dsl_activity_root)

        val container = addChildrenV2()
        val textView = container.findViewById<TextView>(ID_HTML_VIEW)

        textView.text = createWebPage()
    }

    private fun addChildrenV2(): ViewGroup {

        val ll = linearLayout {

            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            orientation = VERTICAL

            textView {

                linearLayoutParams {
                    width = dp_i(200f)
                    height = WRAP_CONTENT
                    gravity = CENTER
                }

                background = fromDrawable(R.drawable.background_tv_rounded_red)
                text = from(R.string.tv_first)
                padTop = dp_i(10f)
                padBottom = dp_i(10f)
                gravity = CENTER
                textColor = Color.WHITE
            }

            textView {

                linearLayoutParams {
                    width = MATCH_PARENT
                    height = WRAP_CONTENT
                    gravity = START
                }

                background = fromDrawable(R.drawable.background_tv_rounded_yellow)
                text = from(R.string.tv_second)
                padLeft = dp_i(10f)
                padTop = dp_i(10f)
                padBottom = dp_i(10f)
                textColor = Color.WHITE
                multiLine = true
                id = ID_HTML_VIEW
            }
        }
        rootView.addView(ll)

        return ll
    }

    private fun createWebPage() : String {

        return  html {

            +"<!-- Any comment inside HTML -->"

            head {
                title { +"This is a Title" }
            }

            body {

                +"<!-- It's a BODY -->"

                h1 {+"XML encoding with Kotlin"}
                p  {+"this format can be used as an alternative markup to XML"}
                b { +"Should be the bold text" }

                a(href = "http://kotlinlang.org") {+"Kotlin"}

                p {
                    +"This is some"
                    b {+"mixed"}
                    +"text. For more see the"
                    a(href = "http://kotlinlang.org") {+"Kotlin"}
                    +"project"
                }
                p {+"some text"}

            }
        }.toString()
    }

    private fun html(init: HTML.() -> Unit): HTML {
        return HTML().apply(init)
    }

    private fun addChildrenV1() {
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
}