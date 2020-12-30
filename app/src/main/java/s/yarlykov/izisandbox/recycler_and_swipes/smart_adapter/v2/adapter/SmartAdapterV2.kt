package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.adapter

import android.util.SparseArray
import android.view.ViewGroup
import androidx.core.util.set
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.controller.BaseController
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.holder.BaseViewHolder
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.item.BaseItem
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.model.SmartList

class SmartAdapterV2 : RecyclerView.Adapter<BaseViewHolder>() {

    /**
     * Модель данных
     */
    private val model = SmartList()

    /**
     * Список контроллеров для текущей модели данных
     */
    private val supportedControllers = SparseArray<BaseController<*, *>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return supportedControllers[viewType].createViewHolder(parent)
    }

    /**
     * Короче вот этот ад с явным кастом и type projection позволил запуститься.
     */
    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        // Вот этот код не запуститься, потому что автоматическая type projection
        // блокирует.
//        model[position].let { item ->
//            item.controller.bind(holder, item)
//        }

        val baseItem = model[position] as BaseItem<BaseViewHolder>
        val controller =
            baseItem.controller as BaseController<in BaseViewHolder, in BaseItem<BaseViewHolder>>

        controller.bind(holder, baseItem)
    }

    override fun getItemViewType(position: Int): Int {
        return model[position].controller.viewType()
    }

    override fun getItemCount(): Int = model.size


    /**
     * Обновляем модель данных
     */
    fun updateModel(smartList: SmartList) {

        model.apply {
            clear()
            addAll(smartList)
        }

        // Обновить контроллеры
        updateControllers()
        notifyDataSetChanged()
    }

    /**
     * Обновляем модель контроллеров
     */
    private fun updateControllers() {
        supportedControllers.apply {
            clear()
            model.forEach { item -> this[item.controller.viewType()] = item.controller }
        }
    }
}