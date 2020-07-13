package s.yarlykov.izisandbox.transitions.shared_with_activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import s.yarlykov.izisandbox.R

class ActivitySharedFrom : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shared_from)

        findViewById<View>(R.id.view_red).apply {
            isClickable = true
            isFocusable = true
            setOnClickListener(onClickListener)
        }
    }

    private val onClickListener: (View) -> Unit = { view ->

        val intent = Intent(this@ActivitySharedFrom, ActivitySharedTo::class.java)

        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this@ActivitySharedFrom,
            view,
            getString(R.string.shared_transition_red_ball)
        )
        startActivity(intent, options.toBundle())
    }
}