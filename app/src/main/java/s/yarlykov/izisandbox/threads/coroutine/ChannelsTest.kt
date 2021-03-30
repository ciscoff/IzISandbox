package s.yarlykov.izisandbox.threads.coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import java.util.concurrent.Executors
import kotlin.coroutines.suspendCoroutine

suspend fun testBuilder(scope: CoroutineScope) {

//    scope.launch {
//        println("scope=$scope")
//    }
//
//    suspendCoroutine<Unit> {
//        println("continuation=$it, context=${it.context}")
//    }

//    suspendCoroutine<Unit> {
//        (it as CoroutineScope).launch {
//            println("Yo Yo")
//        }
//    }

    suspendCoroutine<Unit> {
        (it.context[Job] as CoroutineScope).launch {
            println("Yo Yo")
        }
    }
}

/**
 *
 */

fun main() {



    /**
     * Для чистоты эксперимента создаем свой пул
     */
    val pool = Executors.newFixedThreadPool(5).asCoroutineDispatcher()

    val data = 5


    runBlocking {

        testBuilder(this)

        val channel = Channel<Int>()

        launch(pool) {
            delay(300)
            println("send $data '${Thread.currentThread().name}'")
            channel.send(data)
            println("send done '${Thread.currentThread().name}'")
        }

        launch(Dispatchers.IO) {
            delay(1000)
            println("receive is starting '${Thread.currentThread().name}'")
            val i = channel.receive()
            println("receive done with $i '${Thread.currentThread().name}'")
        }
    }
}