package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter

import android.util.SparseArray
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.controller.ItemControllerBase
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.item.ItemBase

class SmartAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<ItemBase<RecyclerView.ViewHolder>>()
    private val supportedItemControllers =
        SparseArray<ItemControllerBase<RecyclerView.ViewHolder, ItemBase<RecyclerView.ViewHolder>>>()

    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return supportedItemControllers[viewType].createViewHolder(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, adapterPosition: Int) {
        val position = getListPosition(adapterPosition)

        val item = items[position]
        item.itemController.bind(holder, item)

    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return items[getListPosition(position)].itemController.viewType
    }

    /**
     * В оригинале вариант сложнее
     */
    private fun getListPosition(adapterPosition : Int) : Int = adapterPosition
}


