package s.yarlykov.izisandbox.threads.coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.suspendCoroutine


suspend fun rightUse() {

    coroutineScope {

        println("rightUse:1 " + this.toString().substringAfterLast("@"))

        launch {
            println("rightUse:2 " + this.toString().substringAfterLast("@"))
            println("hello from rightUse")
        }
    }

}

suspend fun testUse() {
    suspendCoroutine<Unit> { cont ->

        println(
            "testUse:1 " + (cont.context[Job] as CoroutineScope).toString().substringAfterLast("@")
        )

        (cont.context[Job] as CoroutineScope).launch {
            println("testUse:2 " + this.toString().substringAfterLast("@"))
            println("hello from testUse")
        }
    }
}


fun coroutineScopeTest() {

    CoroutineScope(Job()).launch {

        launch(Dispatchers.IO) {
            println("main:testUse:1 " + this.toString().substringAfterLast("@"))
            testUse()
        }

        launch {
            println("main:rightUse:1 " + this.toString().substringAfterLast("@"))
            rightUse()
        }
    }

}

@ExperimentalCoroutinesApi
fun main() {

    /** Flow можно создать без всякого scope. */
    val someFlow = flow {
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
            }
            channel.consumeEach {
                emit(it)
            }
        }
    }

    val chFlow = channelFlow {
        send(1)
        send(2)
    }

    CoroutineScope(Job()).launch {

        someFlow.collect {
            println("next: $it")
        }
    }

    Thread.sleep(5000)
}