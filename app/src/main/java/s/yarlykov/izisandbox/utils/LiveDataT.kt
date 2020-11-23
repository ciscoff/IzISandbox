package s.yarlykov.izisandbox.utils

import androidx.lifecycle.MutableLiveData

open class LiveDataT<T>(private val default: T) : MutableLiveData<T>() {
    init {
        value = default
    }

    override fun getValue(): T {
        return super.getValue() ?: default
    }

    var valueOrDefault
        get() : T? = value
        set(value) {
            setValue(value ?: default)
        }
}