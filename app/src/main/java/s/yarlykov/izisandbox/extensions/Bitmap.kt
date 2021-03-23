package s.yarlykov.izisandbox.extensions

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface

/**
 * Повернуть картинку, чтобы она встала вертикально в области просмотра.
 *
 * Угол 0 - это когда во время съемки телефон был в горизонтальном положении и
 * кнопкой ON/OFF вверх. Если телефон во время съемки поставить вертикально, так чтобы
 * ON/OFF оказалась справа, то это поворот 90 CW (или ExifInterface.ORIENTATION_ROTATE_90)
 *
 * Получается так: у фотографии два размера - ширина и высота и при этом шириной является
 * наибольший размер. То есть у фотоаппарата ширина всегда больше высоты в каком бы положении
 * он не находился. Если держать телефон вертикально, то для нас фотка как-бы вертикальная, а
 * фотик считает, что это его ширина повернулась на 90 градусов CW (exif). И в результате
 * фотография сохраняется таким образом, что "наша высота" является "шириной фотика", но
 * делается пометка, что съемка проводилась при повороте на 90CW.
 *
 * Если попытаться просмотреть вертикально сделанную фотку и проигнорировать exif, то она
 * отобразится "заваленной" на бок (словно повернуляь на 90 CCW), как бы в горизонтальной
 * раскладке. Но если мы хотим чтобы все было ОК, то должны создать свою bitmap, дать ей ширину
 * равную высоте фотика и высоту равную ширине фотика и повернуть исходную битмапу на 90CW.
 * Что мы и делаем далее.
 */
fun Bitmap.rotate(orientation: Int): Bitmap {

    val angle = orientationToAngle(orientation)

    return if (angle != 0) {
        val matrix = Matrix().apply { postRotate(angle.toFloat()) }
        Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    } else {
        this
    }
}

/**
 * Получить "конечные" размеры битмапы для состояния когда она уже правильно
 * повернута на экране с учетом
 */
fun BitmapFactory.Options.oriented(orientation: Int): Pair<Int, Int> {
    return if (orientationToAngle(orientation) % 180 == 0)
        outWidth to outHeight
    else
        outHeight to outWidth
}

/**
 * Конвертер
 */
fun orientationToAngle(orientation: Int): Int {
    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }
}