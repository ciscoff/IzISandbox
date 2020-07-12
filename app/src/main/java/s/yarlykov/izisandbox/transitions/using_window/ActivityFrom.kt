package s.yarlykov.izisandbox.transitions.using_window

/**
 * Важно: https://stackoverflow.com/questions/37607973/setentertransition-only-works-with-activitycompat-startactivity
 */
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.Explode
import android.view.Window
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import s.yarlykov.izisandbox.R

class ActivityFrom : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        setupWindowAnimations()

        with(window) {
//            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            setContentView(R.layout.activity_from)
//            enterTransition = Fade(Fade.IN).apply {
//                duration = 500
//            }

            exitTransition = Explode().apply {
                duration = 500
            }
        }


        findViewById<LinearLayout>(R.id.activity_from_root_id).apply {
            setOnClickListener {
                ActivityTo.startNew(
                    this@ActivityFrom
                )
            }
        }
    }

    companion object {
        fun startNew(context : Context) {
            context.startActivity(Intent(context, ActivityFrom::class.java))

        }
    }
}