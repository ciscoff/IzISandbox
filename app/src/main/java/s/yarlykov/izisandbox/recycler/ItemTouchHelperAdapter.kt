package s.yarlykov.izisandbox.recycler

interface ItemTouchHelperAdapter {
    fun onItemDismiss(position : Int)
    fun onItemMove(fromPosition : Int, toPosition : Int)
}