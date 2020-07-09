package s.yarlykov.izisandbox

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Explode
import android.transition.Fade
import android.transition.TransitionInflater
import android.view.Window

class ActivityGraph2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setupWindowAnimations()

        with(window) {
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)

            enterTransition = Fade(Fade.IN).apply {
                duration = 500
            }
            exitTransition = Explode()
        }

        setContentView(R.layout.activity_graph2)
    }

    private fun setupWindowAnimations() {
        val fade = TransitionInflater.from(this).inflateTransition(R.transition.activity_fade)
        window.enterTransition = fade
    }
}