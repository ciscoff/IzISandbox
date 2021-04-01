package s.yarlykov.izisandbox.matrix.avatar_maker_prod.scale

import android.graphics.PointF

interface ScaleController {
    fun onScaleRequired(factor: Float, pivot: PointF)
    fun onScaleDownAvailable(isAvailable: Boolean)
    fun onScaleUpAvailable(isAvailable: Boolean)

    fun onBackSizeChanged(size: Pair<Int, Int>)
    fun onFrontSizeChanged(size: Pair<Int, Int>)

    fun rotateCcw()

    var bitmapScaleCurrent: Float
    var bitmapScaleMin: Float
}