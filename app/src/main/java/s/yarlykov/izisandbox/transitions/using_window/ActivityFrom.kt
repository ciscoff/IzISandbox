package s.yarlykov.izisandbox.transitions.using_window

/**
 * Важно: https://stackoverflow.com/questions/37607973/setentertransition-only-works-with-activitycompat-startactivity
 */
import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.transition.Explode
import android.transition.Fade
import android.transition.Slide
import android.view.Gravity
import android.view.Window
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.showSnackBarNotification

/**
 * Данная активити запускается из другой актитиви и это позволяет нам
 * определить анимацию, с которой данная актитиви выйдет на экран.
 *
 * Для этого нужно.
 * - до setContentView вызвать window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
 * - там же установить желаемую анимацию
 * - после этого вызывать setContentView
 *
 * А запускать данную активити нужно через методом, который используется в companion
 *
 */
class ActivityFrom : AppCompatActivity() {

    private lateinit var viewModel: ViewModelActivityFrom
    private lateinit var rootView: LinearLayout

    // Чтобы snack bar показать только раз
    private var isReentered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val animDuration = resources.getInteger(R.integer.animation_activity_in_out).toLong()
        /**
         * Нужно делать до вызова setContentView(layout_id)
         */
        with(window) {
            requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)

            enterTransition = Fade(Fade.IN).apply {
                duration = animDuration
            }

            /**
             * Сработает если из данной актитиви запустить другую актитити. То есть
             * стартуем ActivityTo и уходим с экрана с эффектом Explode()
             */
            exitTransition = Explode().apply {
                duration = animDuration
            }
            /**
             * Сработает когда мы вернемся сюда из ActivityTo. Это как бы "вторая рука"
             * предыдущего действия exitTransition. Эти транзиции спаренные.
             * Вот спаренные транзиции:
             * - EnterTransition <--> ReturnTransition
             * - ExitTransition <--> ReenterTransition
             *
             * Обращаю внимание, что спаренность работает только, если выполняются соответствующие
             * спаренные действия: запустили другую активити и затем вернулись из неё.
             */
            reenterTransition = Slide(Gravity.END)
        }

        setContentView(R.layout.activity_from)
        rootView = findViewById(R.id.activity_from_root_id)

        viewModel = ViewModelProvider(this).get(ViewModelActivityFrom::class.java)

        viewModel.isReentrant.observe(this, Observer {
            isReentered = it
        })

        findViewById<LinearLayout>(R.id.activity_from_root_id).apply {
            setOnClickListener {
                ActivityTo.startNew(
                    this@ActivityFrom
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (!isReentered) {
            Handler().postDelayed(
                { rootView.showSnackBarNotification(getString(R.string.notification_click_anywhere)) },
                500
            )
        } else {
            viewModel.hasEntered()
        }
    }

    override fun onPause() {
        super.onPause()
        isReentered = true
    }

    companion object {

        fun startNew(context: Context) {
            val intent = Intent(context, ActivityFrom::class.java)
            context.startActivity(
                intent,
                ActivityOptions.makeSceneTransitionAnimation(context as Activity).toBundle()
            )
        }
    }
}