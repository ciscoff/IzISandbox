package s.yarlykov.izisandbox.matrix.surface

import android.view.SurfaceHolder
import android.view.SurfaceView

class DrawingThread(
    private val view : SurfaceView,
    private val holder : SurfaceHolder,
    private val monitor: Monitor

) : Thread() {
    override fun run() {
        super.run()
    }
}