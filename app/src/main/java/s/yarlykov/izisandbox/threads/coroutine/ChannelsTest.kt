package s.yarlykov.izisandbox.threads.coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

fun main() {

    CoroutineScope(Job()).launch {

        val channel = Channel<Int>()

        launch(Dispatchers.Unconfined) {
            delay(300)
            println("send 5. thread:${Thread.currentThread().name}")
            channel.send(5)
            println("send, done. thread:${Thread.currentThread().name}\"")
        }

        launch(Dispatchers.Default) {
            delay(1000)
            println("receive. thread:${Thread.currentThread().name}\"")
            val i = channel.receive()
            println("receive $i, done. thread:${Thread.currentThread().name}\"")
        }
    }

    Thread.sleep(3000)
}