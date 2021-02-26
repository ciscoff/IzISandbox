package s.yarlykov.izisandbox.threads.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.random.Random

fun main() {

    val scope = CoroutineScope(Job())

    scope.launch {

        val job1 = launch {
            TimeUnit.MILLISECONDS.sleep(5500)
        }

        val job2 = launch {
            TimeUnit.MILLISECONDS.sleep(1000)
        }

        job2.join()
        job1.join()
    }

    Thread.sleep(10000)
}
