package s.yarlykov.izisandbox.matrix.surface.v02

import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_smooth_dragging.*
import s.yarlykov.izisandbox.R

class SmoothDraggingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_smooth_dragging)
    }

    override fun onResume() {
        super.onResume()
        surface.resume()
    }

    override fun onPause() {
        super.onPause()
        surface.pause()
    }
}