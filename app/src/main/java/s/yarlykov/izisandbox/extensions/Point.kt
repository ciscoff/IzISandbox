package s.yarlykov.izisandbox.extensions

import android.graphics.PointF

val PointF.invalid: Boolean
    get() = x == Float.MAX_VALUE ||
            x == Float.MIN_VALUE ||
            y == Float.MIN_VALUE ||
            y == Float.MAX_VALUE
