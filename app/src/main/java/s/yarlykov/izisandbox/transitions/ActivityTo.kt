package s.yarlykov.izisandbox.transitions

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import s.yarlykov.izisandbox.R

class ActivityTo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setupWindowAnimations()

//        with(window) {
//            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
//            requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
//
//            enterTransition = Fade(Fade.IN).apply {
//                duration = 500
//            }
//            exitTransition = Explode()
//        }

        setContentView(R.layout.activity_to)
    }
}