package s.yarlykov.izisandbox.threads.coroutine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*

/**
 * Как работают операторы, применяемый в цепочке Flow.
 */

fun main() = runBlocking<Unit> {

    val flowStrings = flow {
        // Это лямда-generator
        emit("abc")
        emit("def")
        emit("ghi")
    }
    // 1. Итак, flowStrings это:
    // flowStrings = SafeFlow(generator: FlowCollector.() -> Unit)
    // Теперь нужно передать в flowStrings коллектор. см шаг 3.

    // 2. Ещё один SafeFlow со своим 'generator: FlowCollector.() -> Unit'
    val flowUpperCase = flow {

        // 3. flowStrings.collect{...} создаст ананонимный коллектор, через который получит/прогонит
        // все строки из flowStrings и передаст их в блок лямда-consumer-1. Этот блок принимает строки
        // и тут же отправляет их в другой коллектор, который есть this у enclosed лямды !!!
        flowStrings.collect { str ->
            // Это лямда-consumer-1
            emit(str.toUpperCase(Locale.ROOT))
        }
    }

    launch(Dispatchers.IO) {

        // 4. flowUpperCase.collect {...} создаст ананонимный коллектор, через который прогонит
        // все строки из flowUpperCase и передаст их в блок лямда-consumer-2.
        flowUpperCase.collect {
            // Это лямда-consumer-2
            delay(300)
            println(it)
        }
    }
}