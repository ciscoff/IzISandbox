package s.yarlykov.izisandbox.threads.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.random.Random

fun main() {
    /**
     * Здесь создается некий SafeFlow (он же Flow), который в конструкторе получает нашу лямду
     * с emit'ами, точнее с вызовами emit'ов. Потом эта лямда будет запущена на созданом
     * FlowCollector'е и будут вызваны реализации ЕГО emit'ов. То есть здесь просто вызовы, а реализация
     * будет в новом FlowCollector'е. В данный момент этого коллектора ещё нет. Но он будет создан
     * далее в строке flowStrings.collect { println(it) }. Там функция collect создаст новый
     * FlowCollector и реализация его emit'а будет отправлять данные из emit'ов нашей лямды в
     * пользовательскую { println(it) }. Вот и все ))
     *
     * NOTE: То есть фактически здеь ничего не генерится. Это просто заготовка.
     */
    val flowStrings = flow {
        emit("abc ${Thread.currentThread().name}")
        emit("def")
        emit("ghi")
    }

    val scope = CoroutineScope(Job())

    scope.launch {
        flowStrings.collect { println(it) }

//        f.collect { println(it.toString()) }

//        val job1 = launch {
//            TimeUnit.MILLISECONDS.sleep(5500)
//        }
//
//        val job2 = launch {
//            TimeUnit.MILLISECONDS.sleep(1000)
//        }
//
//        job2.join()
//        job1.join()
    }

    Thread.sleep(120000)
}
