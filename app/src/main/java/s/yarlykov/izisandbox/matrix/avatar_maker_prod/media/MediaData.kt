package s.yarlykov.izisandbox.matrix.avatar_maker_prod.media

import android.graphics.Bitmap
import android.graphics.Rect

data class MediaData(
    val bitmap: Bitmap,
    val viewPort: Rect,
    val bitmapVisibleHeightMin: Float,
    val bitmapScaleMin: Float
)