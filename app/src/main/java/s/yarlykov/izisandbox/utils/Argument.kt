package s.yarlykov.izisandbox.utils

import android.os.Bundle
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 *
 */
class Argument<Arg : Any>(
    private val argumentProducer: () -> Bundle
) : ReadOnlyProperty<Any, Arg> {

    private var cached: Arg? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): Arg {
        var args = cached
        if (args == null) {
            val arguments = argumentProducer()

            @Suppress("UNCHECKED_CAST")
            args = arguments.get(property.name) as Arg
            cached = args
        }
        return args
    }
}

@MainThread
inline fun <reified Arg : Any> Fragment.args() = Argument<Arg> {
    arguments ?: throw IllegalStateException("Fragment $this has null arguments")
}