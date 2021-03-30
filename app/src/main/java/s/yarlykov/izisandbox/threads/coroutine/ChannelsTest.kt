package s.yarlykov.izisandbox.threads.coroutine

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.Executors
import kotlin.coroutines.suspendCoroutine

@ExperimentalCoroutinesApi
suspend fun testBuilder(scope: CoroutineScope) {

    Observable.create<Int> { emitter ->
        (1..10).forEach(emitter::onNext)
    }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    flow<Int> {
        coroutineScope {
            val channel = produce<Int> {
                launch {
                    delay(1000)
                    send(1)
                }
                launch {
                    delay(1000)
                    send(2)
                }
                launch {
                    delay(1000)
                    send(3)
                }
            }
            channel.consumeEach {
                // ...
            }
        }
    }

    val flow = channelFlow {
        launch {
            delay(1000)
            send(1)
        }
        launch {
            delay(1000)
            send(2)
        }
        launch {
            delay(1000)
            send(3)
        }
    }




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