package s.yarlykov.izisandbox.rx.test_rx

import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

class CustomSubscriber : Subscriber<Int> {

    var subscription : Subscription? = null

    override fun onSubscribe(s: Subscription?) {
        subscription = s
        println("onSubscribe: subscription ${subscription != null}")
        s?.request(1)
    }

    override fun onNext(t: Int?) {
        println("onNext $t")
        try {
            Thread.sleep(100L)
        } catch (e : Exception) {
            println("Exception")
        }
        subscription?.request(1)
    }

    override fun onError(t: Throwable?) {
        println("onError")
    }

    override fun onComplete() {
        println("onComplete")
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
        .range(1, 10)
        .subscribeOn(Schedulers.newThread())
        .observeOn(Schedulers.io())
        .subscribe(CustomSubscriber())

    try {
        println("Thread '${Thread.currentThread().name}' is sleeping for 3 secs")
        Thread.sleep(3000L)
    } catch (e : Exception) {

    }
}