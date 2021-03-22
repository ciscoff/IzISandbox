package s.yarlykov.izisandbox.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import s.yarlykov.izisandbox.extensions.cameraOrientation
import s.yarlykov.izisandbox.extensions.rotate
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object PhotoHelper {

    /**
     * Создать JPG-файл с уникальным именем и префиксом "avatar_<time_stamp>"
     */
    @Throws(IOException::class)
    fun createImageFile(context: Context): File {

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(
            "avatar_${timeStamp}", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }

    /**
     * Уменьшить размер картинки в 2 раза.
     * @src - Uri исходного файла
     * @dest - Path целевого файла
     *
     * При работе с фотографией с камеры @src/@dest ссылаются на один и тот же файл.
     * При работе с картинкой из галлереи - это разные файлы.
     * @dest всегда располагается внутри каталога приложения.
     *
     * NOTE: Есть нюанс: аватарки приходят повернутые на -90 (против часовой), если
     * фотка тыльной камерой и на +90 (по часовой), если фронтальной. Это при вертикальном положении
     * телефона. Перед показом аватарку нужно развернуть правильно.
     * https://stackoverflow.com/questions/3647993/android-bitmaps-loaded-from-gallery-are-rotated-in-imageview
     * http://sylvana.net/jpegcrop/exif_orientation.html
     * https://guides.codepath.com/android/Accessing-the-Camera-and-Stored-Media#rotating-the-picture
     */
    fun reduceImageFile(context: Context, sourceUri: Uri, destPath: String): Boolean {

        var originalBitmap: Bitmap? = null

        return try {

            originalBitmap =
                context.contentResolver.openInputStream(sourceUri)?.use {
                    BitmapFactory.decodeStream(it)
                }

            originalBitmap
                ?.reduce()
                ?.cropCenter()
                ?.rotate(context.cameraOrientation(sourceUri))
                ?.writeToStorage(destPath) ?: false
        } catch (e: IOException) {
            false
        } finally {
            originalBitmap?.recycle()
        }
    }

    /**
     * Bitmap уменьшенная в ratio раз.
     */
    private fun Bitmap.reduce(ratio: Int = 2): Bitmap =
        if (height > width) {
            scaleToFitHeight(height / ratio)
        } else {
            scaleToFitWidth(width / ratio)
        }

    private fun Bitmap.scaleToFitWidth(w: Int): Bitmap {
        val factor = w / width.toFloat()
        return Bitmap.createScaledBitmap(this, w, (height * factor).toInt(), true)
    }

    private fun Bitmap.scaleToFitHeight(h: Int): Bitmap {
        val factor = h / height.toFloat()
        return Bitmap.createScaledBitmap(this, (width * factor).toInt(), h, true)
    }

    /**
     * Записать битмапу в JPEG-формате в файл @path
     */
    private fun Bitmap.writeToStorage(path: String): Boolean {

        return ByteArrayOutputStream().use { bytes ->
            this.compress(Bitmap.CompressFormat.JPEG, 50, bytes)

            try {
                FileOutputStream(File(path)).use { fos ->
                    fos.write(bytes.toByteArray())
                    true
                }
            } catch (e: IOException) {
                false
            }
        }
    }

    /**
     * Center Crop
     */
    private fun Bitmap.cropCenter(): Bitmap {
        return if (width >= height) {
            Bitmap.createBitmap(
                this,
                width / 2 - height / 2,
                0,
                height,
                height
            )

        } else {
            Bitmap.createBitmap(
                this,
                0,
                height / 2 - width / 2,
                width,
                width
            )
        }
    }
}
