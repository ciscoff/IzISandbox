package s.yarlykov.izisandbox.rx.test_rx

import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

/**
 * Демонстрация работы backpressure. Оператор range (он же FlowableRange) генерит
 * некоторое количество элементов, заполняя свой буфер (похоже, что его размер 128 элементов).
 * Затем подписчик начинает забирать оттуда по одному элементу через каждые 5 ms и когда в буфере
 * генератора остается ~32 элемента, то он снова заполняет его и генерит дополнительные 96
 * элементов. Ну и т.д.
 */

var startTime = 0L

class CustomSubscriber : Subscriber<Int> {

    var subscription : Subscription? = null

    override fun onSubscribe(s: Subscription?) {
        startTime = System.currentTimeMillis()
        subscription = s
        println("${System.currentTimeMillis() - startTime}: onSubscribe")
        s?.request(1)
    }

    override fun onNext(t: Int?) {
        println("${System.currentTimeMillis() - startTime}: onNext $t")
        try {
            Thread.sleep(5L)
        } catch (e : Exception) {
            println("Exception")
        }
        subscription?.request(1)
    }

    override fun onError(t: Throwable?) {
        println("${System.currentTimeMillis() - startTime}: onError")
    }

    override fun onComplete() {
        println("${System.currentTimeMillis() - startTime}: onComplete")
    }
}

/**
 * Метод subscribe обернет мой CustomSubscriber() в инстанс StrictSubscriber<Int>, который
 * наследует от FlowableSubscriber и Subscription. То есть когда Subscriber запрашивает
 * у источника очередную порцию данных через request(n), то он вызывает свой метод
 * StrictSubscriber.request(..), а не метод источника !!!
 */
fun main(args: Array<String>) {

    Flowable
        .range(1, 1000)
        .doOnNext {
            println("${System.currentTimeMillis() - startTime}: FlowableRange post item $it")
        }
        .subscribeOn(Schedulers.newThread())
        .observeOn(Schedulers.io())
        .subscribe(CustomSubscriber())

    try {
        println("Thread '${Thread.currentThread().name}' is sleeping for 3 secs")
        Thread.sleep(10000L)
    } catch (e : Exception) {
    }
}