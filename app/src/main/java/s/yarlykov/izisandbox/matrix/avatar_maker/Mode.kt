package s.yarlykov.izisandbox.matrix.avatar_maker

sealed class Mode {
    object Dragging : Mode()
    sealed class Scaling : Mode() {
        object Init : Scaling()
        object Shrink : Scaling()
        object Squeeze : Scaling()
    }
}
