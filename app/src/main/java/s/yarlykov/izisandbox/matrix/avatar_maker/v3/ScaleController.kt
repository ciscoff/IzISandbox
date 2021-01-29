package s.yarlykov.izisandbox.matrix.avatar_maker.v3

import android.graphics.PointF

interface ScaleController {
    fun onScaleRequired(factor: Float, pivot: PointF)
    fun onScaleDownAvailable(isAvailable: Boolean)
    fun onScaleUpAvailable(isAvailable: Boolean)
}