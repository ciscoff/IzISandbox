package s.yarlykov.izisandbox.dsl

import android.content.Context
import android.view.View
import android.view.ViewGroup

inline fun <reified TV : View> vc(context: Context, init: TV.() -> Unit): TV {
    val constr = TV::class.java.getConstructor(Context::class.java)
    val view = constr.newInstance(context)
    view.init()

    return view
}

inline fun <reified TV : View> vp(parent: ViewGroup, init: TV.() -> Unit): TV {
    val constr = TV::class.java.getConstructor(Context::class.java)
    val view = constr.newInstance(parent.context)
    parent.addView(view)

    // init нужно вызывать после parent.addView, потому что addView применяет к view
    // LayoutParams, которые в init можно настраивать.
    view.init()

    return view
}