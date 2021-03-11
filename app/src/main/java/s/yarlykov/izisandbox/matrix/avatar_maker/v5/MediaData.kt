package s.yarlykov.izisandbox.matrix.avatar_maker.v5

import android.graphics.Bitmap
import android.graphics.Rect

data class MediaData(
    val bitmap: Bitmap,
    val viewPort: Rect,
    val bitmapVisibleHeightMin: Float,
    val bitmapScaleMin: Float
)