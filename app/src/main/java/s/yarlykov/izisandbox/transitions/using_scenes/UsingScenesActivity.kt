package s.yarlykov.izisandbox.transitions.using_scenes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Scene
import android.transition.Transition
import android.transition.TransitionInflater
import android.transition.TransitionManager
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import s.yarlykov.izisandbox.R

class UsingScenesActivity : AppCompatActivity() {

    private lateinit var scene0 : Scene
    private val buttons = ArrayList<Button>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_using_scenes)
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    private fun findViews() {
        
    }

    private fun setupWindowAnimations() {
        window.enterTransition =
            TransitionInflater.from(this).inflateTransition(R.transition.slide_left_transition)

        window.enterTransition.addListener(object  : Transition.TransitionListener() {
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
}