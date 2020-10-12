package s.yarlykov.izisandbox.telegram.v2

import android.content.Context
import android.util.TypedValue

const val TAG_DEBUG = "TAG_DEBUG"
const val NOT_FIRST_ON_TOP = -1

val Context.actionBarSize : Float
get() {
    val tv = TypedValue()

    return if(theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
        TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics).toFloat()
    }
    else {
        0f
    }
}