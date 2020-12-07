package s.yarlykov.izisandbox.matrix.surface

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.Window
import kotlinx.android.synthetic.main.activity_surface_drawing.*
import s.yarlykov.izisandbox.R

class SurfaceDrawingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_surface_drawing)
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