package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v1

import android.util.SparseArray
import android.view.ViewGroup
import androidx.core.util.set
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v1.controller.ItemControllerBase
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v1.model.ItemList
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v1.model.ItemBase
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v1.vh.ViewHolderBase

class SmartAdapter : RecyclerView.Adapter<ViewHolderBase>() {

    /**
     * Модель
     */
    private val items = mutableListOf<ItemBase<*>>()

    /**
     * Контроллеры. Хранят layoutId и отвечают за создание viewHolder'ов и их биндинг с данными.
     */
    private val supportedItemControllers =
        SparseArray<ItemControllerBase<ViewHolderBase, ItemBase<ViewHolderBase>>>()

    /**
     * Создание viewHolder'а делегируется контроллеру
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderBase {
        return supportedItemControllers[viewType].createViewHolder(parent)
    }

    /**
     * Binding делегируется контроллеру
     */
    override fun onBindViewHolder(holder: ViewHolderBase, adapterPosition: Int) {
        val position = getListPosition(adapterPosition)
        val item = items[position]
        item.itemController.bind(holder, item)
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return items[getListPosition(position)].itemController.viewType
    }

    /**
     * См. оригинальный метод
     */
    private fun getListPosition(adapterPosition: Int): Int = adapterPosition

    /**
     * Обновить модель
     */
    fun updateItems(list: ItemList) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    /**
     * Обновить списко контроллеров
     */
    @Suppress("UNCHECKED_CAST")
    fun updateControllers(list: ItemList) {
        supportedItemControllers.clear()

        list.forEach { item ->
            val controller = item.itemController as ItemControllerBase<ViewHolderBase, ItemBase<ViewHolderBase>>
            supportedItemControllers[controller.viewType] = controller
        }
    }
}


