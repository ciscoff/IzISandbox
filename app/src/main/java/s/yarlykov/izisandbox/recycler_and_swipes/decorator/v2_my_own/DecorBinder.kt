package s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own

/**
 * Контейнер DecorBinder связывает viewType с декоратором отдельного типа.
 */
class DecorBinder<D>(val viewType: Int, val decorator: D)