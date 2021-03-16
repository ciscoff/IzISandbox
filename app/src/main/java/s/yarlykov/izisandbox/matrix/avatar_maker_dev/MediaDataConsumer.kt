package s.yarlykov.izisandbox.matrix.avatar_maker_dev

import android.graphics.Bitmap

interface MediaDataConsumer {
    fun onBitmapReady(bitmap: Bitmap)
}