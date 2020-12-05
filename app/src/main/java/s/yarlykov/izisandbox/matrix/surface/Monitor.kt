package s.yarlykov.izisandbox.matrix.surface

import android.graphics.PointF
import s.yarlykov.izisandbox.utils.logIt

/**
 * Kotlin: wait() и notify() отсутствуют в Any. Поэтому делаем так:
 * https://ru.stackoverflow.com/questions/853006/kotlin-wait-and-notify
 * + https://stackoverflow.com/questions/35520583/why-there-are-no-concurrency-keywords-in-kotlin
 */
class Monitor {
    private var isValueSet = false
    private val lock = Object()

    var center: PointF = PointF()
        get() = synchronized(lock) {

            while (!isValueSet) {
                try {
                    lock.wait()
                } catch (e: InterruptedException) {
                    logIt("InterruptedException caught")
                }
            }
            isValueSet = false
            lock.notify()
            return@synchronized field
        }
        set(value) = synchronized(lock) {

            while (isValueSet) {
                try {
                    lock.wait()
                } catch (e: InterruptedException) {
                    logIt("InterruptedException caught")
                }
            }
            field = value
            isValueSet = true
            return@synchronized
        }
}