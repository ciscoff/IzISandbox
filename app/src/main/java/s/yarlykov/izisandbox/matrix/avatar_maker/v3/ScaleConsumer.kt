package s.yarlykov.izisandbox.matrix.avatar_maker.v3

import android.graphics.PointF

interface ScaleConsumer {

    /**
     * Сообщения разрешающие/запрещающие запускать анимацию
     */
    fun onScaleDownAvailable(isAvailable: Boolean)
    fun onScaleUpAvailable(isAvailable: Boolean)

    /**
     * Подготовиться к анимации
     */
    fun onPreScale(factor: Float, pivot: PointF)

    /**
     * Выполнить отдельную итерацию внутри анимационного цикла
     */
    fun onScale(fraction: Float)
}