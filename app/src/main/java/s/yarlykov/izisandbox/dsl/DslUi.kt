package s.yarlykov.izisandbox.dsl

import android.content.Context
import android.view.View
import android.view.ViewGroup

/**
 *  -------------------------------------------------------------------------------------
 *  Step 1. Context и ViewGroup являются аргументами функций.
 *  -------------------------------------------------------------------------------------
 */
inline fun <reified TV : View> vc(context: Context, init: TV.() -> Unit): TV {
    val constr = TV::class.java.getConstructor(Context::class.java)
    val view = constr.newInstance(context)

    view.init()

    return view
}

/**
 * view.init() нужно вызывать после parent.addView(), потому что addView применяет к view
 * LayoutParams, которые в init можно настраивать.
 */
inline fun <reified TV : View> vp(parent: ViewGroup, init: TV.() -> Unit): TV {
    val constr = TV::class.java.getConstructor(Context::class.java)
    val view = constr.newInstance(parent.context)

    parent.addView(view)
    view.init()

    return view
}

/**
 *  -------------------------------------------------------------------------------------
 *  Step 2. Функции переделаны в extension-функции
 *  -------------------------------------------------------------------------------------
 */
inline fun <reified TV : View> Context.vc2(init: TV.() -> Unit): TV {
    val constr = TV::class.java.getConstructor(Context::class.java)
    val view = constr.newInstance(this)

    view.init()

    return view
}

inline fun <reified TV : View> ViewGroup.vp2(init: TV.() -> Unit): TV {
    val constr = TV::class.java.getConstructor(Context::class.java)
    val view = constr.newInstance(context)

    addView(view)
    view.init()

    return view
}