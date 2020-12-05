package s.yarlykov.izisandbox.matrix.surface

import android.view.SurfaceView

class DrawingThread(
    private val view : SurfaceView,
    private val monitor: Monitor
) : Thread() {

    private var isRunning = false

    fun startThread() {
        isRunning = true
        super.start()
    }

    fun stopThread() {
        isRunning = false
    }

    override fun run() {

        while (isRunning) {

            val point = monitor.center
        }

    }
}