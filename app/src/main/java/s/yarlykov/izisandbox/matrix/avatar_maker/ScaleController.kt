package s.yarlykov.izisandbox.matrix.avatar_maker

import android.graphics.PointF

interface ScaleController {
    fun onScaleRequired(factor: Float, pivot: PointF)
    fun onScaleDownAvailable(isAvailable: Boolean)
    fun onScaleUpAvailable(isAvailable: Boolean)

    /**
     * Применяется при увеличении rectBitmapVisible.height в сторону sourceImageBitmap.height.
     * scaleMax имеет значение >= 1. Если обе высоты равны, то scaleMax = 1
     */
    var scaleMax: Float

    /**
     * Применяется при уменьшении rectBitmapVisible.height в сторону rectBitmapVisibleHeightMin.
     * scaleMin имеет значение <= 1.
     */
    var scaleMin: Float
}