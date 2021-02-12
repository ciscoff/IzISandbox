package s.yarlykov.izisandbox.rx

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign

/**
 * Класс источника. Источник может быть локальным или удаленным.
 */
class Source(data: List<String>) : List<String> by data {

    operator fun invoke(): Observable<List<String>> {
        return Observable.create { consumer ->

            if (isEmpty()) {
                consumer.onError(Throwable("No data"))
            } else {
                consumer.onNext(this)
                consumer.onComplete()
            }
        }
    }
}

/**
 * Непосредственно кэш. Запрашивает из локального источника и при отсутствии в нем данных
 * обращается к удаленному источнику.
 */
data class RxCache(val localSource: Source, val remoteSource: Source) {

    fun getData(): Single<List<String>> {
        return localSource().onErrorResumeNext(remoteSource()).firstOrError()
    }
}

fun main() {

    val disposable = CompositeDisposable()

    val cacheNotEmpty = RxCache(
        Source(listOf("1_from_cache", "2_from_cache", "3_from_cache")),
        Source(listOf("1_remote", "2_remote", "3_remote")),
    )

    val cacheEmpty = RxCache(
        Source(emptyList()),
        Source(listOf("1_remote", "2_remote", "3_remote")),
    )

    println("\nWorking with not empty cache")
    disposable += cacheNotEmpty.getData().subscribe(
        { list ->
            list.forEach { println(it) }
        },
        { t -> println("Error: ${t.message}") }
    )

    println("\nWorking with empty cache")
    disposable += cacheEmpty.getData().subscribe(
        { list ->
            list.forEach { println(it) }
        },
        { t -> println("Error: ${t.message}") }
    )
}