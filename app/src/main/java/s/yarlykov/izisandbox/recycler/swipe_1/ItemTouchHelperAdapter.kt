package s.yarlykov.izisandbox.recycler.swipe_1

interface ItemTouchHelperAdapter {
    fun onItemDismiss(position : Int)
    fun onItemMove(fromPosition : Int, toPosition : Int)
}