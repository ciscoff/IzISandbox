package s.yarlykov.izisandbox.recycler_and_swipes.decorator.app.controller

/**
 * Признак "залипающего" элемента. Поле id соответствует adapterPosition и работает как
 * идентификатор, а также позволяет  выполнять поиск в списке элементов StickyHolder по
 * убыванию/возрастанию.
 */
interface StickyHolder {
    val id: Int
}