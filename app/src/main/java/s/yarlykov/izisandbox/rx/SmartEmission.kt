package s.yarlykov.izisandbox.rx

import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit


enum class RunMode {
    Stub,
    Prod,
    PreProd
}

fun main(args: Array<String>) {

    val interval = Observable.interval(2, TimeUnit.SECONDS)
    val source =
        Observable.fromArray(
            RunMode.PreProd,
            RunMode.Prod,
            RunMode.Prod,
            RunMode.PreProd
        )

    val emission = Observable.zip(
        interval,
        source,
        BiFunction<Long, RunMode, RunMode> { _, mode ->
            println("next emission in ${System.currentTimeMillis()}, mode =${mode}")
            mode
        }
    )

    emission.scan (RunMode.Stub) { prev, next ->
        println("prev=$prev, next=$next")
        next
    }.subscribe()

    /**
     * .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
     */

    Thread.sleep(20000)
}