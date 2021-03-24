package s.yarlykov.izisandbox.threads.coroutine

import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
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

fun main() {

    val scope = CoroutineScope(Job() + Dispatchers.IO)

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

        coroutineScope {

        }
    }
}