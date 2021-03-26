package s.yarlykov.izisandbox.threads.coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.Executors

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