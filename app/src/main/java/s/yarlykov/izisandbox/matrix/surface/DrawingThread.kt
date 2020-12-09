package s.yarlykov.izisandbox.matrix.surface

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