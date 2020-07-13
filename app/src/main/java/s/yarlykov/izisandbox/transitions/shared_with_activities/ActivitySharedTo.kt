package s.yarlykov.izisandbox.transitions.shared_with_activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import s.yarlykov.izisandbox.R

class ActivitySharedTo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shared_to)

        findViewById<View>(R.id.view_red).apply {
            isClickable = true
            isFocusable = true
            setOnClickListener {
                this@ActivitySharedTo.supportFinishAfterTransition()
            }
        }
    }
}