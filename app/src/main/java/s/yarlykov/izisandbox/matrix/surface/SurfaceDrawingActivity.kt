package s.yarlykov.izisandbox.matrix.surface

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.Window
import s.yarlykov.izisandbox.R

class SurfaceDrawingActivity : AppCompatActivity() {

    lateinit var surface : SurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_surface_drawing)
    }
}