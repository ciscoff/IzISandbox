package s.yarlykov.izisandbox.matrix.avatar_maker

import android.graphics.Bitmap

interface MediaDataConsumer {
    fun onBitmapReady(bitmap: Bitmap)
}