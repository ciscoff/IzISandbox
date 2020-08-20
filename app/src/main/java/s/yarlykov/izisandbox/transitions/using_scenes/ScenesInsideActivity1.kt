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

class ScenesInsideActivity1 : AppCompatActivity(), View.OnClickListener {

    private lateinit var scene0: Scene
    private lateinit var scene1: Scene
    private lateinit var scene2: Scene
    private lateinit var scene3: Scene
    private lateinit var scene4: Scene
    private lateinit var sceneRoot: ViewGroup
    private lateinit var buttons: List<Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Анимации для входа и выхода из активити
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

        findViews()

        setupLayout()
    }

    private fun findViews() {
        sceneRoot = findViewById<FrameLayout>(R.id.scene_root) as ViewGroup
        scene1 = Scene.getSceneForLayout(sceneRoot, R.layout.layout_scene_1, this)
        scene2 = Scene.getSceneForLayout(sceneRoot, R.layout.layout_scene_2, this)
        scene3 = Scene.getSceneForLayout(sceneRoot, R.layout.layout_scene_3, this)
        scene4 = Scene.getSceneForLayout(sceneRoot, R.layout.layout_scene_4, this)

        buttons = listOf(
            R.id.sample_scene_button1,
            R.id.sample_scene_button2,
            R.id.sample_scene_button3,
            R.id.sample_scene_button4
        ).map {
            findViewById<Button>(it).apply { setOnClickListener(this@ScenesInsideActivity1) }
        }
    }

    override fun onClick(v: View) {

        when (v.id) {
            R.id.sample_scene_button1 -> {
                TransitionManager.go(
                    scene1,
                    ChangeBounds().apply {
                        duration = resources.getInteger(R.integer.anim_duration_turtle).toLong()
                    })
            }
            R.id.sample_scene_button2 -> {
                TransitionManager.go(
                    scene2,
                    TransitionInflater.from(this)
                        .inflateTransition(R.transition.slide_and_changebounds)
                )
            }
            R.id.sample_scene_button3 -> {
                TransitionManager.go(
                    scene3,
                    TransitionInflater.from(this)
                        .inflateTransition(R.transition.slide_and_changebounds_sequential)
                )
            }
            R.id.sample_scene_button4 -> {
                TransitionManager.go(
                    scene4,
                    TransitionInflater.from(this)
                        .inflateTransition(R.transition.slide_and_changebounds_sequential_with_interpolators)
                )
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

        // Появление этой активити
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

        // Возврат в предыдущую активити
        window.returnTransition = Fade(Fade.OUT).apply {
            duration = animDuration
            addTarget(R.id.floating_button)
            addTarget(R.id.app_bar)
            addTarget(R.id.buttons_group)
        }
    }

    private fun setupLayout() {

        scene0 = Scene.getSceneForLayout(sceneRoot, R.layout.layout_scene_0, this).apply {
            setEnterAction {
                for((index, view) in buttons.withIndex()) {
                    view
                        .animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .startDelay = (index * Delay)
                }
            }
        }
    }

    companion object {

        private const val Delay = 100L

        fun startNew(context: Context) {
            val intent = Intent(context, ScenesInsideActivity1::class.java)
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