package s.yarlykov.izisandbox.matrix.avatar_maker

import android.graphics.PointF

interface ScaleConsumerV5 {

    /**
     * Сообщения разрешающие/запрещающие запускать анимацию
     */
    fun onScaleDownAvailable(isAvailable: Boolean)
    fun onScaleUpAvailable(isAvailable: Boolean)

    /**
     * Подготовиться к анимации
     */
    fun onPreAnimate(scaleFactor: Float, pivot: PointF)

    /**
     * Выполнить отдельную итерацию внутри анимационного цикла
     */
    fun onAnimate(fraction: Float)

    /**
     * Действия после окончания анимации
     */
    fun onPostAnimate()
}