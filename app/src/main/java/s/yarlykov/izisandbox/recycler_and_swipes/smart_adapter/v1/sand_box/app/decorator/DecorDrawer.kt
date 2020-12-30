package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v1.sand_box.app.decorator

/**
 * Контейнер DecorBinder связывает viewType с декоратором отдельного типа.
 */
class DecorBinder<D>(val viewType: Int, val decorator: D)