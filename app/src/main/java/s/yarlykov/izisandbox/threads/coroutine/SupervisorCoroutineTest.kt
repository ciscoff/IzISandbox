package s.yarlykov.izisandbox.threads.coroutine

import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

/**
 * Здесь создаем вложенные корутины, их job'ы помещаем в массив, а потом достаем оттуда
 * и смотрим статус в разых ситуациях.
 *
 * При исключении в job3 только его корутина станет не Active.
 */
suspend fun testSupervisorCoroutine(jobs: Array<Job?>) {
    println("context inside ${object {}.javaClass.enclosingMethod?.name}: $coroutineContext")

    val handler = CoroutineExceptionHandler { _, _ ->
        println("Handle exception inside CoroutineExceptionHandler")
        println("job1 is active: ${jobs[0]?.isActive}, job2 is active: ${jobs[1]?.isActive}, job3 is active: ${jobs[2]?.isActive}")
    }

    try {

        supervisorScope {
            println("context inside supervisorScope '${object {}.javaClass.enclosingMethod?.name}':${this.coroutineContext}")

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

            val job3 = launch(handler) {
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
    }
}

fun main() {

    val scope = CoroutineScope(Job() + Dispatchers.IO)

    val jobs = Array<Job?>(3) { null }

    scope.launch(Dispatchers.IO) {
        println("context inside launch '${object {}.javaClass.enclosingMethod?.name}': $coroutineContext")
        testSupervisorCoroutine(jobs)
        println("supervisorScope is finished")

        withContext(Dispatchers.Main) {

        }
    }

    Thread.sleep(5000)
}