package s.yarlykov.izisandbox.matrix.avatar_maker_prod.media

import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import s.yarlykov.izisandbox.extensions.orientationToAngle

/**
 * Класс хранит оригинальные размеры и ориентацию камеры
 */
class BitmapOptions(
    private val rawWidth: Int,
    private val rawHeight: Int,
    val bitmapPath: String? = null,
    val bitmapUri: Uri? = null,
    val orientation: Int = ExifInterface.ORIENTATION_UNDEFINED
) {
    // Размеры, "повернутые" по orientation
    val oriented: Pair<Int, Int>
        get() = if (orientationToAngle(orientation) % 180 == 0)
            rawWidth to rawHeight
        else
            rawHeight to rawWidth
}