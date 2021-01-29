package s.yarlykov.izisandbox.matrix.avatar_maker.v3

import android.graphics.PointF

interface ScaleConsumer {

    /**
     * Сообщение разрешающее/запрещающее запускать анимацию
     */
    fun onScaleAvailable(isAvailable: Boolean)

    /**
     * Подготовиться к анимации
     */
    fun onPreScale(factor: Float, pivot: PointF)

    /**
     * Выполнить отдельную итерацию внутри анимационного цикла
     */
    fun onScale(fraction: Float)
}