package s.yarlykov.izisandbox.dsl

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

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

/**
 *  -------------------------------------------------------------------------------------
 *  Step 3. Прячем generic-функции за реализациями
 *  -------------------------------------------------------------------------------------
 */

fun Context.linearLayout(init: LinearLayout.() -> Unit) = vc2(init)
fun Context.frameLayout(init: FrameLayout.() -> Unit) = vc2(init)
fun ViewGroup.linearLayout(init: LinearLayout.() -> Unit) = vp2(init)
fun ViewGroup.frameLayout(init: FrameLayout.() -> Unit) = vp2(init)

fun Context.textView(init: TextView.() -> Unit) = vc2(init)
fun ViewGroup.textView(init: TextView.() -> Unit) = vp2(init)

/**
 *  -------------------------------------------------------------------------------------
 *  Дополнительные расширения
 *  -------------------------------------------------------------------------------------
 */
fun View.linearLayoutParams(init: LinearLayout.LayoutParams.() -> Unit) {
    layoutParams = (layoutParams as LinearLayout.LayoutParams).apply(init)
}

fun View.frameLayoutParams(init: FrameLayout.LayoutParams.() -> Unit) {
    layoutParams = (layoutParams as FrameLayout.LayoutParams).apply(init)
}

