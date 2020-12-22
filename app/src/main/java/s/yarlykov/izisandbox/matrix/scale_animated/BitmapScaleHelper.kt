package s.yarlykov.izisandbox.matrix.scale_animated

/**
 * https://guides.codepath.com/android/Working-with-the-ImageView
 */
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION

object BitmapScaleHelper {

    enum class Fit {
        Width,
        Height
    }

    /**
     * Масштабирование Bitmap'ы по заданной ширине
     */
    private fun Bitmap.scaleToFitWidth(width: Int): Bitmap {
        val factor = width / this.width.toFloat()
        return Bitmap.createScaledBitmap(this, width, (this.height * factor).toInt(), true)
    }

    /**
     * Масштабирование Bitmap'ы по заданной высоте
     */
    private fun Bitmap.scaleToFitHeight(height: Int): Bitmap {
        val factor = height / this.height.toFloat()
        return Bitmap.createScaledBitmap(this, (this.width * factor).toInt(), height, true)
    }

    fun Bitmap.scaleToFit(fit: Fit, size: Int): Bitmap {
        return when (fit) {
            Fit.Width -> scaleToFitWidth(size)
            Fit.Height -> scaleToFitHeight(size)
        }
    }

    /**
     * Загрузка большой bitmap'ы с понижением resolution до размеров View, в которой она должна
     * отображаться.
     *
     * + https://stackoverflow.com/questions/32121058/most-memory-efficient-way-to-resize-bitmaps-on-android
     */
    fun loadSampledBitmapFromResource(
        context: Context,
        resourceId: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap {

        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeResource(context.resources, resourceId, this)

            // Как будем скалировать: по наименьшему соотношению. "Наименьшее" - значит
            // меньше работы нужно проделать. Например, если reqWidth/outWidth = 0.9, а
            // reqHeight/outHeight = 0.3, то выберем скалирование 0.9, так как размеры ближе
            // друг к другу.
            val fit = if (reqWidth / outWidth > reqHeight / outHeight) Fit.Width else Fit.Height

            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            inJustDecodeBounds = false
            BitmapFactory
                .decodeResource(context.resources, resourceId, this)
                .scaleToFit(fit, reqWidth)
                .rotate(cameraOrientation(context, drawableUri(context, resourceId)))
        }
    }

    /**
     * Во сколько раз нужно изменить размер битмапы при загрузке. Количество раз - это
     * 2 в какой-то степени (положительной или отрицательной)
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {

        val (rawHeight: Int, rawWidth: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (rawHeight > reqHeight || rawWidth > reqWidth) {

            val halfHeight: Int = rawHeight / 2
            val halfWidth: Int = rawWidth / 2

            do {
                inSampleSize *= 2
            } while (
                halfHeight / inSampleSize >= reqHeight &&
                halfWidth / inSampleSize >= reqWidth
            )
        }

        return inSampleSize
    }

    /**
     * Определить ориентацию камеры в битмапе.
     */
    private fun cameraOrientation(context: Context, src: Uri): Int {

        return try {
            context.contentResolver.openInputStream(src)?.use { inputStream ->
                ExifInterface(inputStream).getAttributeInt(TAG_ORIENTATION, 0)
            } ?: 0

        } catch (e: Exception) {
            0
        }
    }

    /**
     * Создаем Uri для битмапы из ресурсов. Например, имеем R.drawable.batumi и на выходе получим
     * "android.resource://s.yarlykov.izisandbox/drawable/batumi"
     */
    private fun drawableUri(context: Context, resourceId: Int): Uri {
        val resources = context.resources

        return (Uri.Builder())
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(resourceId))
            .appendPath(resources.getResourceTypeName(resourceId))
            .appendPath(resources.getResourceEntryName(resourceId))
            .build()
    }

    /**
     * Повернуть картинку, чтобы она встала вертикально в области просмотра.
     */
    private fun Bitmap.rotate(orientation: Int): Bitmap {

        val angle = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

        return if (angle != 0) {
            val matrix = Matrix().apply { setRotate(angle.toFloat()) }
            Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
        } else {
            this
        }
    }
}