package s.yarlykov.izisandbox.transitions.using_window

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.Fade
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import s.yarlykov.izisandbox.R

class ActivityTo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setupWindowAnimations()

        with(window) {
//            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)

            setContentView(R.layout.activity_to)
            enterTransition = Fade(Fade.IN).apply {
                duration = 500
            }
//            exitTransition = Explode().apply {
//                duration = 500
//            }
        }
    }

    companion object {
        fun startNew(context : Context) {
            val intent = Intent(context, ActivityTo::class.java)
            context.startActivity(intent,
                ActivityOptions.makeSceneTransitionAnimation(context as Activity).toBundle())
        }
    }
}