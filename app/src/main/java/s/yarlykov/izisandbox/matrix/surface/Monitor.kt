package s.yarlykov.izisandbox.matrix.surface

import android.graphics.Point
import s.yarlykov.izisandbox.utils.logIt

/**
 * Kotlin: wait() и notify() отсутствуют в Any. Поэтому делаем так:
 * https://ru.stackoverflow.com/questions/853006/kotlin-wait-and-notify
 */
class Monitor {
    private var isValueSet = false
    private val lock = Object()

    private var center: Point = Point()
        get() = synchronized(lock) {

            while (!isValueSet) {
                try {
                    lock.wait()
                } catch (e: InterruptedException) {
                    logIt("InterruptedException caught")
                }
                isValueSet = true
            }
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
                isValueSet = false
            }
            field = value
            return@synchronized
        }
}