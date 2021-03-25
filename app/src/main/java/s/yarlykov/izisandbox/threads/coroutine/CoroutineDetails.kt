package s.yarlykov.izisandbox.threads.coroutine

import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun suspendFun1() {
    suspendCoroutine<Unit> { continuation ->
        thread {
            println("suspendFun1 started")
            TimeUnit.SECONDS.sleep(2)
            println("suspendFun1 finished")
            continuation.resume(Unit)
        }
    }
}

suspend fun suspendFun2() {
    suspendCoroutine<Unit> { continuation ->
        thread {
            println("suspendFun2 started")
            TimeUnit.SECONDS.sleep(2)
            println("suspendFun2 finished")
            continuation.resume(Unit)
        }
    }
}

suspend fun suspendFun3() {
    suspendCoroutine<Unit> { continuation ->
        thread {
            println("suspendFun3 started")
            TimeUnit.SECONDS.sleep(2)
            println("suspendFun3 finished")
            continuation.resume(Unit)
        }
    }
}

suspend fun testCoroutineScope(jobs : Array<Job?>) {
    println("context inside testCoroutineScope=$coroutineContext")


    try {

        coroutineScope {

            val job1 = launch {
                delay(2000)
                println("job1 is finished")
            }

            println("started job1")

            val job2 = launch {
                delay(2000)
                println("job2 is finished")
            }

            println("started job2")

            val job3 = launch {
                delay(1000)
                Integer.parseInt("a")
                println("job3 is finished")
            }

            println("started job3")

            jobs[0] = job1
            jobs[1] = job2
            jobs[2] = job3

            println("context inside coroutineScope=${this.coroutineContext}")
            println("job1 is active: ${jobs[0]?.isActive}, job2 is active: ${jobs[1]?.isActive}, job3 is active: ${jobs[2]?.isActive}")
        }
    } catch (e: Exception) {
        println("EXCEPTION !!!")
        println("job1 is active: ${jobs[0]?.isActive}, job2 is active: ${jobs[1]?.isActive}, job3 is active: ${jobs[2]?.isActive}")
    }

//    delay(100)
//    println("job1 is active: ${jobs[0].isActive}, job2 is active: ${jobs[1].isActive}, job3 is active: ${jobs[2].isActive}")

}

fun main() {

    val scope = CoroutineScope(Job() + Dispatchers.IO)

    val jobs = Array<Job?>(3){null}

    scope.launch(Dispatchers.IO) {
        println("context inside launch=${coroutineContext}")
        testCoroutineScope(jobs)
    }

    Thread.sleep(5000)


    /**
     * Чтобы не ставить TimeUnit.SECONDS.sleep(10) в main-потоке используем runBlocking
     */
    runBlocking {

        println("before coroutineScope block")
        coroutineScope {
            launch {
                suspendFun1()
            }
            launch {
                suspendFun2()
            }
        }

        suspendFun3()
        println("after coroutineScope block")
    }
}