package s.yarlykov.izisandbox.matrix.avatar_maker

import android.graphics.PointF

interface ScaleController {
    fun onScaleRequired(factor: Float, pivot: PointF)
    fun onScaleDownAvailable(isAvailable: Boolean)
    fun onScaleUpAvailable(isAvailable: Boolean)

    /**
     * NOTE: scaleShrink используется при визуальном уменьшении картинки, а это Shrink.
     *
     * Применяется при увеличении rectBitmapVisible.height в сторону sourceImageBitmap.height.
     * scaleShrink имеет значение '1 + delta', где delta характеризует разницу между высотами
     * sourceImageBitmap и rectBitmapVisible.
     */
    var scaleShrink: Float

    /**
     * NOTE: scaleSqueeze используется при визуальном увеличении картинки, а это Squeeze.
     *
     * Применяется при уменьшении rectBitmapVisible.height в сторону rectBitmapVisibleHeightMin.
     * scaleSqueeze имеет значение <= 1.
     *
     * При максимальном увеличении битмапы её видимый размер равен rectBitmapVisibleHeightMin.
     * А так как scaleSqueeze - это отношение rectBitmapVisibleHeightMin к rectBitmapVisible.height,
     * то при максимальном зуме scaleSqueeze = 1
     */
    var scaleSqueeze: Float
}