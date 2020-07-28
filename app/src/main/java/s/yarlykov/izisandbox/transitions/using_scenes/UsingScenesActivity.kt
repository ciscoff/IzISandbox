package s.yarlykov.izisandbox.transitions.using_scenes

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.*
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.transitions.using_window.ActivityTo

class UsingScenesActivity : AppCompatActivity() {

    private lateinit var scene0: Scene
    private val buttons = ArrayList<Button>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(window) {
            requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            setupWindowAnimations()
        }

        setContentView(R.layout.activity_using_scenes)
        // Скрываем Status Bar
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setSupportActionBar(findViewById(R.id.toolbar))

        setupLayout()
    }


    private fun setupWindowAnimations() {

        val animDuration = resources.getInteger(R.integer.animation_activity_in_out).toLong()

        window.allowEnterTransitionOverlap = true

        window.enterTransition =
            TransitionInflater
                .from(this)
                .inflateTransition(R.transition.slide_from_bottom)
                .apply {
                    duration = animDuration
                    addTarget(R.id.buttons_group)
                    addTarget(R.id.app_bar)
//                    addTarget(R.id.sample3_button1)
                }

        window.returnTransition = Fade(Fade.OUT).apply {
            duration = animDuration
            addTarget(R.id.buttons_group)
            addTarget(R.id.app_bar)
        }

        window.enterTransition.addListener(object : Transition.TransitionListener {
            override fun onTransitionEnd(transition: Transition?) {
                window.enterTransition.removeListener(this)
                TransitionManager.go(scene0)
            }

            override fun onTransitionResume(transition: Transition?) {
            }

            override fun onTransitionPause(transition: Transition?) {
            }

            override fun onTransitionCancel(transition: Transition?) {
            }

            override fun onTransitionStart(transition: Transition?) {
            }
        })
    }

    private fun setupLayout() {
        val sceneRoot = findViewById<FrameLayout>(R.id.scene_root) as ViewGroup
        scene0 = Scene.getSceneForLayout(sceneRoot, R.layout.layout_scene_0, this).apply {
            setEnterAction {
            }
        }
    }

    companion object {
        fun startNew(context: Context) {
            val intent = Intent(context, UsingScenesActivity::class.java)
            context.startActivity(
                intent,
                ActivityOptions.makeSceneTransitionAnimation(context as Activity).toBundle()
            )
        }
    }
}