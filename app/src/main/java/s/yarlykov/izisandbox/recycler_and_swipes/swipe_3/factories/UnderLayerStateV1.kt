package s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.factories

/**
 * Класс определяет состояние UI элементов нижнего слоя под слайдером
 */
data class UnderLayerStateV1(
    val leftDrawableId: Int,
    val leftTextId: Int? = null,
    val leftIconId: Int? = null,
    val rightDrawableId: Int,
    val rightTextId: Int? = null,
    val rightIconId: Int? = null
)