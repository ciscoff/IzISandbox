package s.yarlykov.izisandbox.recycler_and_swipes.easy_adapter

import android.util.SparseArray
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class EasyAdapter<H : RecyclerView.ViewHolder> : RecyclerView.Adapter<H>() {

    private val items = mutableListOf<BaseItem<H>>()
    private val supportedItemControllers = SparseArray<BaseItemController<*, *>>()


    override fun getItemViewType(position: Int): Int {
        return items.get(getListPosition(position)).itemController.viewType()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): H {
        return supportedItemControllers.get(viewType).createViewHolder(parent) as H
    }

    override fun onBindViewHolder(holder: H, adapterPosition: Int) {
        val position = getListPosition(adapterPosition)
        val item = items[position]

        computeAdditionalItemListParams(item, position, adapterPosition)

        item.itemController.bind(holder, item)
    }

    override fun getItemCount(): Int = items.size

    /**
     * Compute additional params for Item
     *
     * @param item            - BaseItem to add params
     * @param position        - position in ItemList
     * @param adapterPosition - position in EasyAdapter
     */
    private fun computeAdditionalItemListParams(
        item: BaseItem<*>,
        position: Int,
        adapterPosition: Int
    ) {
        item.position = position
        item.adapterPosition = adapterPosition
        val nextIndex = getListPosition(adapterPosition + 1)
        if (nextIndex < items.size) item.nextItem = items[nextIndex]
        val previousIndex = getListPosition(adapterPosition - 1)
        if (previousIndex >= 0) item.previousItem = items[previousIndex]
    }

    private fun getListPosition(adapterPosition: Int): Int {
        return getListPosition(items, adapterPosition)
    }

    private fun getListPosition(items: List<BaseItem<*>>, adapterPosition: Int): Int {
        return getListPositionInternal(items, adapterPosition)
    }

    private fun getListPositionInternal(items: List<BaseItem<*>>, adapterPosition: Int): Int {
        return adapterPosition
    }
}