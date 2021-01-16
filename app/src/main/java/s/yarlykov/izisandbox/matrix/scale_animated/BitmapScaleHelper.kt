package s.yarlykov.izisandbox.matrix.scale_animated

/**
 * https://guides.codepath.com/android/Working-with-the-ImageView
 */
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import s.yarlykov.izisandbox.extensions.cameraOrientation
import s.yarlykov.izisandbox.extensions.drawableUri
import s.yarlykov.izisandbox.extensions.rotate

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
                .rotate(context.run { cameraOrientation(drawableUri(resourceId)) })
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

}