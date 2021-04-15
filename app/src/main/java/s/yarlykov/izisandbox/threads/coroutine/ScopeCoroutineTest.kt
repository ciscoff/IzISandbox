package s.yarlykov.izisandbox.threads.coroutine

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

const val DELAY_LONG = 2000L
const val DELAY_MIDDLE = 1000L
const val DELAY_SHORT = 500L

/**
 * Здесь создаем вложенные корутины, их job'ы помещаем в массив, а потом достаем оттуда
 * и смотрим статус в разых ситуациях.
 */
suspend fun testCoroutineScope(jobs: Array<Job?>) {
    println("context inside ${object {}.javaClass.enclosingMethod?.name}: $coroutineContext")

    try {

        /**
         * coroutineScope и withContext работают одинаково, но withContext позволяет
         * изменить контекст.
         */
        coroutineScope/*withContext(Dispatchers.Default)*/ {
            println("context inside coroutineScope '${object {}.javaClass.enclosingMethod?.name}':${this.coroutineContext}")

            val job1 = launch {
                delay(DELAY_LONG)
                println("job1 is finished")
            }
            println("job1 is started")

            val job2 = launch {
                delay(DELAY_LONG)
                println("job2 is finished")
            }
            println("job2 is started")

            val job3 = launch {
                delay(DELAY_SHORT)
                Integer.parseInt("exception")
                println("job3 is finished")
            }
            println("job3 is started")

            jobs[0] = job1
            jobs[1] = job2
            jobs[2] = job3
            println("job1 is active: ${jobs[0]?.isActive}, job2 is active: ${jobs[1]?.isActive}, job3 is active: ${jobs[2]?.isActive}")
        }
    } catch (e: Exception) {
        println("!!! EXCEPTION !!!")
        println("job1 is active: ${jobs[0]?.isActive}, job2 is active: ${jobs[1]?.isActive}, job3 is active: ${jobs[2]?.isActive}")
    }
}

suspend fun testSuspend(): Long {
    println("testSuspend: job is ${coroutineContext[Job]}")

    return suspendCoroutine { con ->
        println("Hello")
        con.resume(1L)
    }
}

fun main() {

    val scope = CoroutineScope(Job() + Dispatchers.IO)
    println("Before testSuspend call job is = ${scope.coroutineContext[Job]}")
    scope.launch {
        println("Inside launch and before testSuspend call job is = ${this.coroutineContext[Job]}")
        println("Inside launch and before testSuspend call job is = ${coroutineContext[Job]}")
        testSuspend()
    }

//    val jobs = Array<Job?>(3) { null }
//
//    scope.launch(Dispatchers.IO) {
//        println("context inside launch '${object {}.javaClass.enclosingMethod?.name}': $coroutineContext")
//        testCoroutineScope(jobs)
//        println("coroutineScope is finished")
//    }

    Thread.sleep(5000)
}