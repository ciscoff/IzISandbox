package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.base.BaseDataItem
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.base.BaseViewHolder
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.fabric.ViewHolderFabric
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.model.ItemList
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.sand_box.app.OneRowDataItem
import s.yarlykov.izisandbox.utils.logIt

class SmartAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    private val items = ItemList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return ViewHolderFabric.withType(viewType).invoke(parent)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].viewType
    }

    override fun getItemCount(): Int = items.size

    fun updateModel(model : ItemList) {
        items.apply {
            clear()
            addAll(model)
            notifyDataSetChanged()
        }

    }
}