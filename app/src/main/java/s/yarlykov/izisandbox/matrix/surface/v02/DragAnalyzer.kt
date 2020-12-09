package s.yarlykov.izisandbox.matrix.surface.v02

import android.graphics.PointF
import s.yarlykov.izisandbox.utils.logIt
import kotlin.math.abs
import kotlin.math.max

class DragAnalyzer {

    private val roadMap = mutableMapOf<RoadBookType, RoadBook>()

    init {
        roadMap.apply {
            this[RoadBookType.Slow] = RoadBook(5L, Array(8) { PointF() })
            this[RoadBookType.Moderate] = RoadBook(10L, Array(6) { PointF() })
            this[RoadBookType.Fast] = RoadBook(15L, Array(4) { PointF() })
            this[RoadBookType.Instantly] = RoadBook(20L, Array(2) { PointF() })
        }
    }

    fun analyze(start: PointF, end: PointF, elapsedTime: Long): RoadBook {

        val vX = abs(end.x - start.x) / elapsedTime
        val vY = abs(end.y - start.y) / elapsedTime

        val vMax = max(vX, vY)

        logIt("analyze elapsedTime=$elapsedTime, vMax=$vMax", true, "PLPL")

        return when {
            vMax < 0.01f -> {
                generate(start, end, roadMap[RoadBookType.Slow]!!.points)
                roadMap[RoadBookType.Slow]!!
            }
            vMax < 0.4f -> {
                generate(start, end, roadMap[RoadBookType.Moderate]!!.points)
                roadMap[RoadBookType.Moderate]!!
            }
            vMax < 0.8f -> {
                generate(start, end, roadMap[RoadBookType.Fast]!!.points)
                roadMap[RoadBookType.Fast]!!
            }
            else -> {
                generate(start, end, roadMap[RoadBookType.Instantly]!!.points)
                roadMap[RoadBookType.Instantly]!!
            }
        }
    }

    private fun generate(start: PointF, end: PointF, pathPoints : Array<PointF>) {

        val stepX = (end.x - start.x) / pathPoints.size
        val stepY = (end.y - start.y) / pathPoints.size


        (0..pathPoints.lastIndex).forEach { i ->
            pathPoints[i].x = start.x + stepX * (i + 1)
            pathPoints[i].y = start.y + stepY * (i + 1)
//            logIt("pathPoints[$i]=${pathPoints[i]}", true, "PLPL")
        }
    }
}