package s.yarlykov.izisandbox.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

/**
 * Функция, которая делает ZIP для двух LiveData
 * https://medium.com/@gauravgyal/combine-results-from-multiple-async-requests-90b6b45978f7
 */

fun <A, B> zipLiveData(a: LiveData<A>, b: LiveData<B>): LiveData<Pair<A, B>> {
    return MediatorLiveData<Pair<A, B>>().apply {
        var lastA: A? = null
        var lastB: B? = null

        fun update() {

            lastA?.let { la ->
                lastB?.let { lb ->
                    this.value = Pair(la, lb)
                }
            }
        }

        addSource(a) {
            lastA = it
            update()
        }
        addSource(b) {
            lastB = it
            update()
        }
    }
}

fun <A, B, R> zipLiveData(a: LiveData<A>, b: LiveData<B>, zipper: (A, B) -> R): LiveData<R> {
    return MediatorLiveData<R>().apply {

        var lastA: A? = null
        var lastB: B? = null

        fun update() {

            lastA?.let { la ->
                lastB?.let { lb ->
                    this.value = zipper(la, lb)
                }
            }
        }

        addSource(a) {
            lastA = it
            update()
        }
        addSource(b) {
            lastB = it
            update()
        }
    }
}

fun <A, B, C, R> zipLiveData3(a: LiveData<A>, b: LiveData<B>, c: LiveData<C>, zipper: (A, B, C) -> R): LiveData<R> {
    return MediatorLiveData<R>().apply {
        var lastA: A? = null
        var lastB: B? = null
        var lastC: C? = null

        fun update() {

            lastA?.let { la ->
                lastB?.let { lb ->
                    lastC?.let { lc ->
                        this.value = zipper(la, lb, lc)
                    }
                }
            }
        }

        addSource(a) {
            lastA = it
            update()
        }
        addSource(b) {
            lastB = it
            update()
        }
        addSource(c) {
            lastC = it
            update()
        }
    }
}

fun <A, B, C, D, R> zipLiveData4(a: LiveData<A>, b: LiveData<B>, c: LiveData<C>, d: LiveData<D>, zipper: (A, B, C, D) -> R): LiveData<R> {
    return MediatorLiveData<R>().apply {
        var lastA: A? = null
        var lastB: B? = null
        var lastC: C? = null
        var lastD: D? = null

        fun update() {

            lastA?.let { la ->
                lastB?.let { lb ->
                    lastC?.let { lc ->
                        lastD?.let { ld ->
                            this.value = zipper(la, lb, lc, ld)
                        }
                    }
                }
            }
        }

        addSource(a) {
            lastA = it
            update()
        }
        addSource(b) {
            lastB = it
            update()
        }
        addSource(c) {
            lastC = it
            update()
        }

        addSource(d) {
            lastD = it
            update()
        }
    }
}

fun <A, B, C, D, E, R> zipLiveData5(a: LiveData<A>, b: LiveData<B>, c: LiveData<C>, d: LiveData<D>, e: LiveData<E>, zipper: (A, B, C, D, E) -> R): LiveData<R> {
    return MediatorLiveData<R>().apply {
        var lastA: A? = null
        var lastB: B? = null
        var lastC: C? = null
        var lastD: D? = null
        var lastE: E? = null

        fun update() {

            lastA?.let { la ->
                lastB?.let { lb ->
                    lastC?.let { lc ->
                        lastD?.let { ld ->
                            lastE?.let { le ->
                                this.value = zipper(la, lb, lc, ld, le)
                            }
                        }
                    }
                }
            }
        }

        addSource(a) {
            lastA = it
            update()
        }
        addSource(b) {
            lastB = it
            update()
        }
        addSource(c) {
            lastC = it
            update()
        }

        addSource(d) {
            lastD = it
            update()
        }

        addSource(e) {
            lastE = it
            update()
        }
    }
}

fun <A, B, C, D, E, F,  R> zipLiveData6(a: LiveData<A>, b: LiveData<B>, c: LiveData<C>, d: LiveData<D>, e: LiveData<E>, f: LiveData<F>, zipper: (A, B, C, D, E, F) -> R): LiveData<R> {
    return MediatorLiveData<R>().apply {
        var lastA: A? = null
        var lastB: B? = null
        var lastC: C? = null
        var lastD: D? = null
        var lastE: E? = null
        var lastF: F? = null

        fun update() {

            lastA?.let { la ->
                lastB?.let { lb ->
                    lastC?.let { lc ->
                        lastD?.let { ld ->
                            lastE?.let { le ->
                                lastF?.let { lf ->
                                    this.value = zipper(la, lb, lc, ld, le, lf)
                                }
                            }
                        }
                    }
                }
            }
        }

        addSource(a) {
            lastA = it
            update()
        }
        addSource(b) {
            lastB = it
            update()
        }
        addSource(c) {
            lastC = it
            update()
        }

        addSource(d) {
            lastD = it
            update()
        }

        addSource(e) {
            lastE = it
            update()
        }

        addSource(f) {
            lastF = it
            update()
        }
    }
}