package s.yarlykov.izisandbox.transitions.using_scenes

import android.graphics.Color
import android.os.Bundle
import android.transition.Scene
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.dsl.extenstions.from
import s.yarlykov.izisandbox.dsl.extenstions.fromDrawable
import s.yarlykov.izisandbox.dsl.extenstions.textColor
import s.yarlykov.izisandbox.dsl.frameLayoutParams
import s.yarlykov.izisandbox.dsl.textView

class ScenesInsideActivity3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scenes_inside_3)

        val sceneRoot: FrameLayout = findViewById<FrameLayout>(R.id.scene_root_1)
        val sceneContent = findViewById<TextView>(R.id.tv_content_1)
        val sceneEnter1 = findViewById<TextView>(R.id.scene_1_enter)
        val sceneExit1 = findViewById<TextView>(R.id.scene_1_exit)

        val scene1 = Scene(sceneRoot).apply {
            setEnterAction {
                sceneRoot.initContent()
//                sceneContent.visibility = View.VISIBLE
            }
            setExitAction {
                sceneRoot.clearContent()
//                sceneContent.visibility = View.INVISIBLE
            }
        }

        sceneEnter1.setOnClickListener {
            scene1.enter()
        }

        sceneExit1.setOnClickListener {
            scene1.exit()
        }
    }

    private fun FrameLayout.initContent() {

        textView {

            frameLayoutParams {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = ViewGroup.LayoutParams.MATCH_PARENT
                gravity = Gravity.CENTER // layout_gravity для TextView внутри FrameLayout
            }

            background = fromDrawable(R.drawable.background_tv_rounded_red)
            text = from(R.string.tv_first)
            gravity = Gravity.CENTER
            textColor = Color.WHITE
        }
    }

    private fun FrameLayout.clearContent() {
        removeAllViews()
    }
}