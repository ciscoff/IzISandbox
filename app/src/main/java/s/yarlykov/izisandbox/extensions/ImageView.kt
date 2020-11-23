package s.yarlykov.izisandbox.extensions

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import kotlin.math.max


fun ImageView.setRoundedDrawable(drawableId: Int) {
    val resources = context.resources
    val srcBitmap = BitmapFactory.decodeResource(resources, drawableId)

    setImageDrawable(
        RoundedBitmapDrawableFactory.create(resources, srcBitmap).apply {
            cornerRadius = max(srcBitmap.width, srcBitmap.height) / 2.0f
        }
    )
}

fun ImageView.setRoundedDrawable(uri: Uri) {

    if (uri == Uri.EMPTY) return

    context.contentResolver.openInputStream(uri)?.use { stream ->
        setImageDrawable(
            RoundedBitmapDrawableFactory.create(resources, stream).apply {
                cornerRadius = max(this.bitmap!!.width, this.bitmap!!.height) / 2.0f
            }
        )
    }
}

/**
 * Для конвертации ShapeDrawable в Bitmap
 */
fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    }
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}