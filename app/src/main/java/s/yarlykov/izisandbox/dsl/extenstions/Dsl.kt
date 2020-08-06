package s.yarlykov.izisandbox.extensions

import android.util.TypedValue
import android.view.View


/**
 * Конвертация dp в px. px возвращается как Float
 */
fun View.dp_f(dp: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics
    )
}

/**
 * Конвертация dp в px. px возвращается как Int
 */
fun View.dp_i(dp: Float): Int {
    return dp_f(dp).toInt()
}


/* --------------------------------------- */

var View.padLeft: Int
    get() = paddingLeft
    set(value) {
        setPadding(value, paddingTop, paddingRight, paddingBottom)
    }

var View.padTop: Int
    get() = paddingLeft
    set(value) {
        setPadding(paddingLeft, value, paddingRight, paddingBottom)
    }

var View.padRight: Int
    get() = paddingLeft
    set(value) {
        setPadding(paddingLeft, paddingTop, value, paddingBottom)
    }

var View.padBottom: Int
    get() = paddingLeft
    set(value) {
        setPadding(paddingLeft, paddingTop, paddingRight, value)
    }
