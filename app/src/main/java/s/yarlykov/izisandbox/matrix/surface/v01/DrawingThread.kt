package s.yarlykov.izisandbox.matrix.surface.v01

import s.yarlykov.izisandbox.matrix.surface.Monitor
import s.yarlykov.izisandbox.matrix.surface.Renderer

class DrawingThread(private val monitor: Monitor, private val renderer: Renderer) : Thread() {

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
            renderer.render(monitor.point)
        }
    }
}