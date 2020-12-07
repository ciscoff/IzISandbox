package s.yarlykov.izisandbox.matrix.surface

class DrawingThread(
    private val view : CustomSurfaceView
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

            if(view.holder.surface.isValid) {
//                view.render()
            }
        }

    }
}