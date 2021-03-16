package s.yarlykov.izisandbox.extensions

val Pair<Int, Int>.notZero: Boolean
    get() = first != 0 && second != 0