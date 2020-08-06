package s.yarlykov.izisandbox.dsl

import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import s.yarlykov.izisandbox.R

class DslActivity : AppCompatActivity() {

    private lateinit var rootView : FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dsl)

        rootView = findViewById(R.id.dsl_activity_root)

        addChildren()
    }

    private fun addChildren() {
        val ll = vc<LinearLayout>(this) {
            layoutParams = ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            orientation = VERTICAL

            vp<TextView>(this) {
                layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                text = "First child"
            }

            vp<TextView>(this) {
                layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                text = "Second child"
            }
        }

        rootView.addView(ll)
    }
}