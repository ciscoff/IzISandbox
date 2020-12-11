package s.yarlykov.izisandbox.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object PhotoHelper {

    /**
     * Создать JPG-файл с уникальным именем и префиксом "izi_<time_stamp>"
     */
    @Throws(IOException::class)
    fun createImageFile(context: Context): File {

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(
            "izi_${timeStamp}", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }

    /**
     * Уменьшить размер картинки в 2 раза.
     * @src - Uri исходного файла
     * @dest - Path целевого файла
     *
     * При работе с фотограффией с камеры @src/@dest ссылаются на один и тотжже файл.
     * При работе с картинкой из галлереи - это разные файлы.
     * @dest всегда располагается внутри каталоги приложения.
     *
     * NOTE: Есть косяк: аватарки передаются на сервер повернутые на -90 (против часовой), если
     * фотка тыльной камерой и +90 (по часовой), если фронтальной. Это при вертикальном положении
     * телефона. Перед отправкой на сервер аватарку нужно развернуть правильно.
     * https://stackoverflow.com/questions/3647993/android-bitmaps-loaded-from-gallery-are-rotated-in-imageview
     * http://sylvana.net/jpegcrop/exif_orientation.html
     * https://guides.codepath.com/android/Accessing-the-Camera-and-Stored-Media#rotating-the-picture
     */
    fun reduceImageFile(context: Context, src: Uri, dest: String): Boolean {

        var originalBitmap: Bitmap? = null

        return try {

            val cameraOrientation =
                context.contentResolver.openInputStream(src)?.use { inputStream ->
                    ExifInterface(inputStream).getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
                } ?: 0

            originalBitmap =
                context.contentResolver.openInputStream(src)?.use {
                    BitmapFactory.decodeStream(it)
                }

            originalBitmap
                ?.reduce()
                ?.cropCenter()
                ?.rotate(cameraOrientation)
                ?.writeToStorage(dest) ?: false
        } catch (e: IOException) {
            false
        } finally {
            originalBitmap?.recycle()
        }
    }

    /**
     * Определить ориентацию камеры относительно снимаемого объекта и повернуть картинку.
     * В результате картинка всегда встает вертикально в аватарке.
     */
    private fun Bitmap.rotate(orientation: Int): Bitmap {

        val angle = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

        return if (angle != 0) {
            val matrix = Matrix().apply { postRotate(angle.toFloat()) }
            Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
        } else {
            this
        }
    }

    /**
     * Bitmap уменьшенная в ratio раз.
     */
    fun Bitmap.reduce(ratio : Int = 2): Bitmap =
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
