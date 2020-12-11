package s.yarlykov.izisandbox.matrix.surface.v02

import android.graphics.PointF
import s.yarlykov.izisandbox.matrix.surface.Monitor
import s.yarlykov.izisandbox.matrix.surface.Renderer
import s.yarlykov.izisandbox.utils.logIt

/**
 * Итак, точки предоставляет UI, который "снимает показания" тачей каждые 16.67ms (60 fps). Эту
 * периодичность обеспечивает Choreographer.
 *
 * Наш поток получает точки через Monitor и генерит команды для рисования окружности. Далее эти
 * команды уходят на SurfaceFlinger и тот ретранслирует их далее. Положительный момент использования
 * отдельного потока для рисования в том, что он не привязан к UI и может выполнять долгую работу
 * по подготовке рисунка. После окончания этой работы - отправлять результат в SurfaceFlinger.
 *
 * Точно также он может очень быстро формировать новые кадры и кидать их в SurfaceFlinger, ОДНАКО
 * это не значит, что эти кадры будут также быстро отрисовываться на экране !!! Кадры отрисовываются
 * на той же скокрости 60 fps, которую обеспечивает VSYNC и не более того.
 *
 * Поэтому мое первое решение разбивать расстояние между двумя последовательными точками тача
 * на несколько мелких отрезков и последовательно их отрисовывать - ошибочно.
 */

class SmoothDrawingThread(private val monitor: Monitor, private val renderer: Renderer) : Thread() {

    private var isRunning = false

    private var prevPointTime = 0L
    private var prevPoint = PointF()

    private val currentTime: Long
        get() = System.currentTimeMillis()

    fun startThread() {
        isRunning = true
        super.start()
    }

    fun stopThread() {
        isRunning = false
    }

    override fun run() {

        // DEBUG (ловим VSYNC)
        prevPointTime = currentTime

        while (isRunning) {

            // В этот момент поток должен засыпать до следующего VSYNC
            val nextPoint = monitor.point

            // DEBUG (ловим VSYNC)
            logIt("Thread got the point:$nextPoint. Time between points: ${currentTime - prevPointTime} ms")
            prevPointTime = currentTime

            if (nextPoint.x == prevPoint.x && nextPoint.y == prevPoint.y) continue

            renderer.render(nextPoint)
        }
    }
}