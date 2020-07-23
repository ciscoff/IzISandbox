package s.yarlykov.izisandbox.recycler_and_swipes.swipe_1

interface ItemTouchHelperAdapter {
    fun onItemDismiss(position : Int)
    fun onItemMove(fromPosition : Int, toPosition : Int)
}