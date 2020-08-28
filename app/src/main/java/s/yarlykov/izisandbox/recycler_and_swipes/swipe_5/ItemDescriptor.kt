package s.yarlykov.izisandbox.recycler_and_swipes.swipe_5

import s.yarlykov.izisandbox.R

abstract class ItemState(
    val leftColors: List<Int>,
    val rightColors: List<Int>,
    val leftStrings: List<Int>,
    val rightStrings: List<Int>,
    val leftTags: List<Int>,
    val rightTags: List<Int>
)

/**
 * Описание значения атрибутов боковых эелементов в OrderSwipeItem
 */
object ItemDescriptor {

    const val tagConfirm = 100
    const val tagForAll = 101
    const val tagRefuse = 102
    const val tagExecute = 103
    const val tagBack = 104
    const val tagFinish = 105

    val tags = setOf(tagConfirm, tagForAll, tagRefuse, tagExecute, tagBack, tagFinish)

    object state2 : ItemState(
        leftColors = listOf(R.color.colorDecor3, R.color.colorDecor4),
        rightColors = listOf(R.color.colorDecor2),
        leftStrings = listOf(R.string.btn_order_for_all_ex, R.string.btn_order_confirm),
        rightStrings = listOf(R.string.btn_order_refuse),
        leftTags = listOf(tagForAll, tagConfirm),
        rightTags = listOf(tagRefuse)
    )

    object state3 : ItemState(
        leftColors = listOf(R.color.colorDecor8),
        rightColors = listOf(R.color.colorDecor2, R.color.colorDecor12),
        leftStrings = listOf(R.string.btn_order_launch),
        rightStrings = listOf(R.string.btn_order_refuse, R.string.btn_order_to_processing),
        leftTags = listOf(tagExecute),
        rightTags = listOf(tagRefuse, tagBack)
    )

    object state6 : ItemState(
        leftColors = listOf(android.R.color.darker_gray),
        rightColors = emptyList<Int>(),
        leftStrings = listOf(R.string.btn_order_completed),
        rightStrings = emptyList<Int>(),
        leftTags = listOf(tagFinish),
        rightTags = emptyList<Int>()
    )

    fun state(id : Int) : ItemState? {
        return when (id) {
            2 -> state2
            3 -> state3
            6 -> state6
            else -> null
        }
    }
}