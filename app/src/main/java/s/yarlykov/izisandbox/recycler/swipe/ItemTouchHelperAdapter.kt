package s.yarlykov.izisandbox.recycler.swipe

interface ItemTouchHelperAdapter {
    fun onItemDismiss(position : Int)
    fun onItemMove(fromPosition : Int, toPosition : Int)
}