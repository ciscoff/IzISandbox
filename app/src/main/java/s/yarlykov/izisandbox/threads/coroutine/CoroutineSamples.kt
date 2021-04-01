package s.yarlykov.izisandbox.threads.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.random.Random

fun Flow<String>.toUpperCase(): Flow<String> = flow {

    this@toUpperCase.collect {  sz -> // лямда, которая будет телом emit финального коллектора
        emit(sz.toUpperCase())
    }
}

/**

 flow {
    здесь генерим data и вызываем emit(data) на предоставленном коллекторе

    val data = ....
    emit(data)
 }

 Подробнее:
 ----------
 flow {lamda_1} =>
    val safeFlow = new SafeFlow(lamda_1)
    return safeFlow

 Теперь имеем SafeFlow заряженный лямдой, которая сгенерит данные, если её
 запустить на FlowCollector'е. Осталось создать коллектор и предоставить его.
 Это и есть подписка на SafeFlow.

 Затем:
 ----------

 safeFlow.collect { что делать внутри emit с данными (т.е. с аргументом), т.е. тело для emit }

 Подробнее:
 ----------
 srcFlow.collect{lamda_2} =>
   val col = object : FlowCollector() {
                fun emit(data) {
                    call lamda_2(data)
                }
            }
   this.collect(col), где this - это srcFlow

   Теперь srcFlow внутри своей collect возьмет col и запустит на нем lamda_1

   col.lamda_1()

   и lamda_1 будет генерить данные и отправлять их в col.emit(data), которая отправит
   их дальше в lamda_2(data).

 */

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

        flowStrings.toUpperCase().collect {

        }



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
