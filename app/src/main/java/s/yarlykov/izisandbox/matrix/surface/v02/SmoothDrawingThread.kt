package s.yarlykov.izisandbox.matrix.surface.v02

import android.graphics.PointF
import s.yarlykov.izisandbox.matrix.surface.Monitor
import s.yarlykov.izisandbox.matrix.surface.Renderer
import s.yarlykov.izisandbox.utils.logIt

class SmoothDrawingThread(private val monitor: Monitor, private val renderer: Renderer) : Thread() {

    companion object {
        const val REDRAW_INTERVAL = 1L
        const val DRAW_STEPS = 4
    }

    private var isRunning = false
    private var lastPoint = PointF()
    private val pathPoints = Array(DRAW_STEPS) { PointF() }

    private var lastPointTime = 0L

    private val analyzer = DragAnalyzer()

    private val currentTime: Long
        get() = System.nanoTime() / 1_000

    fun startThread() {
        isRunning = true
        super.start()
    }

    fun stopThread() {
        isRunning = false
    }

    override fun run() {

        lastPointTime = currentTime

        while (isRunning) {

            val point = monitor.point

            if (point.x == lastPoint.x && point.y == lastPoint.y) continue

            renderLoop(analyzer.analyze(lastPoint, point, currentTime - lastPointTime))

            lastPointTime = currentTime
            lastPoint.x = point.x
            lastPoint.y = point.y

//            fillPath(point)
//            renderLoop()
        }
    }

    private fun fillPath(point: PointF) {
        logIt(
            "fillPath:start point: ${point.x}, ${point.y}, lastPoint: ${lastPoint.x}, ${lastPoint.y}, offsets: x=${point.x - lastPoint.x}, y=${point.y - lastPoint.y}",
            false,
            "PLPL"
        )

        val stepX = (point.x - lastPoint.x) / pathPoints.size
        val stepY = (point.y - lastPoint.y) / pathPoints.size

        (0..pathPoints.lastIndex).forEach { i ->
            pathPoints[i].x = lastPoint.x + stepX * (i + 1)
            pathPoints[i].y = lastPoint.y + stepY * (i + 1)
            logIt("pathPoints[$i]=${pathPoints[i]}", true, "PLPL")
        }

        lastPoint.x = point.x
        lastPoint.y = point.y
        logIt(
            "fillPath:end point: ${point.x}, ${point.y}, lastPoint: ${lastPoint.x}, ${lastPoint.y}",
            false,
            "PLPL"
        )
    }

    private fun renderLoop() {
        (0..pathPoints.lastIndex).forEach { i ->

            val prevRenderTime = currentTime
            var isContinue: Boolean

            do {
                isContinue = (currentTime - prevRenderTime) < REDRAW_INTERVAL
            } while (isContinue)

            renderer.render(pathPoints[i])
        }
    }

    private fun renderLoop(roadBook: RoadBook) {

        (0..roadBook.points.lastIndex).forEach { i ->

            val prevRenderTime = currentTime
            var isContinue: Boolean

            do {
                isContinue = (currentTime - prevRenderTime) < roadBook.redrawInterval
            } while (isContinue)

            renderer.render(roadBook.points[i])
        }

    }
}