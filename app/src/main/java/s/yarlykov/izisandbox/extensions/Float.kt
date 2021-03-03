package s.yarlykov.izisandbox.extensions

fun Float.normalize(): Float = if (this == -0.0f) 0.0f else this
