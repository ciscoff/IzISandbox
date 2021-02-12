package s.yarlykov.izisandbox.matrix.avatar_maker

import android.graphics.PointF

interface ScaleController {
    fun onScaleRequired(factor: Float, pivot: PointF)
    fun onScaleDownAvailable(isAvailable: Boolean)
    fun onScaleUpAvailable(isAvailable: Boolean)

    var scaleMax : Float
    var scaleMin : Float
}