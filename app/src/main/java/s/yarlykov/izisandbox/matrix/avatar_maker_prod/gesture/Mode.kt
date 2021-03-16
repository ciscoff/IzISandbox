package s.yarlykov.izisandbox.matrix.avatar_maker_prod.gesture

/**
 * Модель состояний
 */
sealed class Mode {
    object Waiting : Mode()
    object Dragging : Mode()
    object Animating : Mode()
    sealed class Scaling : Mode() {
        object Init : Scaling()
        object Shrink : Scaling()
        object Squeeze : Scaling()
    }
}
