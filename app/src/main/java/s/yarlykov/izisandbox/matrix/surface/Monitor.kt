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

    var point: PointF = PointF()
        get() {
            synchronized(lock) {
                // Пока не положили новое значение - ждем
                while (!isValueSet) {
                    try {
                        lock.wait()
                    } catch (e: InterruptedException) {
                        logIt("InterruptedException caught")
                    }
                }
                isValueSet = false
                lock.notify()
                return field
            }
        }
        set(value) {
            synchronized(lock) {
                // Пока не забрали предыдущее значение - ждем
                while (isValueSet) {
                    try {
                        lock.wait()
                    } catch (e: InterruptedException) {
                        logIt("InterruptedException caught")
                    }
                }
                field.x = value.x
                field.y = value.y
                isValueSet = true
                lock.notify()
            }
        }

    fun wakeUpSleeping() {
        isValueSet = !isValueSet
        lock.notify()
    }
}