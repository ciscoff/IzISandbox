package s.yarlykov.izisandbox.matrix.avatar_maker

import android.graphics.PointF

interface ScaleControllerV5 {
    fun onScaleRequired(factor: Float, pivot: PointF)
    fun onScaleDownAvailable(isAvailable: Boolean)
    fun onScaleUpAvailable(isAvailable: Boolean)

    fun onBackSizeChanged(size: Pair<Int, Int>)
    fun onFrontSizeChanged(size: Pair<Int, Int>)

    var bitmapScaleCurrent: Float
    var bitmapScaleMin: Float
}