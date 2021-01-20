package s.yarlykov.izisandbox.extensions

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.TypedValue
import android.view.View
import androidx.exifinterface.media.ExifInterface

/**
 * Прочитать ресурс dimen и вернуть его значение в px
 */
fun Context.dimensionPix(dimenId: Int): Int {
    return this.resources.getDimensionPixelOffset(dimenId)
}

val Context.screenWidth: Int
    get() = resources.displayMetrics.widthPixels

val Context.screenHeight: Int
    get() = resources.displayMetrics.heightPixels

/**
 * Конвертация dp в px. px возвращается как Float
 */
fun Context.dp_f(dp: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics
    )
}

/**
 * Создаем Uri для битмапы из ресурсов. Например, имеем R.drawable.batumi и на выходе получим
 * "android.resource://s.yarlykov.izisandbox/drawable/batumi"
 */
fun Context.drawableUri(resourceId: Int): Uri {

    return (Uri.Builder())
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(resources.getResourcePackageName(resourceId))
        .appendPath(resources.getResourceTypeName(resourceId))
        .appendPath(resources.getResourceEntryName(resourceId))
        .build()
}

/**
 * Определить ориентацию камеры в битмапе.
 */
fun Context.cameraOrientation(src: Uri): Int {

    return try {
        contentResolver.openInputStream(src)?.use { inputStream ->
            ExifInterface(inputStream).getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
        } ?: 0

    } catch (e: Exception) {
        0
    }
}