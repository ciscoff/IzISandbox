package s.yarlykov.izisandbox.transitions.shared_with_activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import s.yarlykov.izisandbox.R

class ActivitySharedTo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shared_to)

        findViewById<View>(R.id.view_colored).apply {
            isClickable = true
            isFocusable = true
            background = ContextCompat.getDrawable(this@ActivitySharedTo, fetchBackground())
            setOnClickListener {
                this@ActivitySharedTo.supportFinishAfterTransition()
            }
        }
    }

    private fun fetchBackground(): Int {
        return intent?.getIntExtra(
            KEY_BACKGROUND,
            R.drawable.shape_oval_red
        ) ?: R.drawable.shape_oval_red
    }

    companion object {
        const val KEY_BACKGROUND = "KEY_BACKGROUND"
    }
}