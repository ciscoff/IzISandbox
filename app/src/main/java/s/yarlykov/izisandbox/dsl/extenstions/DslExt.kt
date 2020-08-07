package s.yarlykov.izisandbox.dsl.extenstions

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat


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

/**
 * При вызове infix функций нужно явно указывать receiver слева. Если receiver неявно
 * присутствует (некий this), то его нужно явно указать как this.
 */
infix fun Context.from(id: Int): String {
    return resources.getString(id)
}

infix fun View.from(id: Int): String {
    return resources.getString(id)
}

infix fun View.fromDrawable(id: Int): Drawable? {
    return ContextCompat.getDrawable(context, id)
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

var TextView.textColor: Int
    get() = currentTextColor
    set(value) {
        setTextColor(value)
    }