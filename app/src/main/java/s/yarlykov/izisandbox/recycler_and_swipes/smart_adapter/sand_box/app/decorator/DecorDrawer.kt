package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.sand_box.app.decorator

/**
 * Контейнер DecorBinder связывает viewType с декоратором отдельного типа.
 */
class DecorBinder<D>(val viewType: Int, val decorator: D)