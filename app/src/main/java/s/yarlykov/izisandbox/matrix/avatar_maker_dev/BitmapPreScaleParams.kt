package s.yarlykov.izisandbox.matrix.avatar_maker_dev

import android.graphics.PointF

data class BitmapPreScaleParams(
    val pivot: PointF,
    val pivotRatioX: Float,
    val pivotRatioY: Float,
    val startWidth: Int,
    val startHeight: Int
)