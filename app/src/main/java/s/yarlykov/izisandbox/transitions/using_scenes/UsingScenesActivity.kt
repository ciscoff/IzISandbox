package s.yarlykov.izisandbox.transitions.using_scenes

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.*
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import s.yarlykov.izisandbox.R

class UsingScenesActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var scene0: Scene
    private lateinit var scene1: Scene
    private lateinit var scene2: Scene
    private lateinit var scene3: Scene
    private lateinit var scene4: Scene
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

    override fun onClick(v: View) {

        when(v.id) {
            R.id.sample_scene_button1 -> {

            }
            R.id.sample_scene_button2 -> {

            }
            R.id.sample_scene_button3 -> {

            }
            R.id.sample_scene_button4 -> {

            }
        }
    }

    /**
     * Внутри макета прочитай коммент по поводу атрибута android:transitionGroup="true" для
     * ViewGroup, благодаря которому ViewGroup анимируется при входе в активити. Без этого
     * флага не анимируется, а просто отрисовывается статически.
     */
    private fun setupWindowAnimations() {

        val animDuration = resources.getInteger(R.integer.animation_activity_in_out).toLong()

        window.allowEnterTransitionOverlap = true

        window.enterTransition =
            TransitionInflater
                .from(this)
                .inflateTransition(R.transition.slide_from_bottom)
                .apply {
                    duration = animDuration
                    addTarget(R.id.floating_button)
                    addTarget(R.id.app_bar)
                    addTarget(R.id.buttons_group)
                    addListener(transitionListener)
                }

        window.returnTransition = Fade(Fade.OUT).apply {
            duration = animDuration
            addTarget(R.id.floating_button)
            addTarget(R.id.app_bar)
            addTarget(R.id.buttons_group)
        }
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

    private val transitionListener = object : Transition.TransitionListener {
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
    }
}