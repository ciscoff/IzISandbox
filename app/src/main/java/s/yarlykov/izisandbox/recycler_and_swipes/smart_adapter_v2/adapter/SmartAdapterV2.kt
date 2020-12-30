package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.adapter

import android.util.SparseArray
import android.view.ViewGroup
import androidx.core.util.set
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.controller.BaseController
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.holder.BaseViewHolder
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.item.BindableItem
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.model.SmartList
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.sand_box.Controller1
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.sand_box.TextModel
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.sand_box.ViewHolder1

class SmartAdapterV2 : RecyclerView.Adapter<BaseViewHolder>() {

    /**
     * Модель данных
     */
    private val model = SmartList() // Array<BaseItem<*>>

    /**
     * Список контроллеров для текущей модели данных
     */
    private val supportedControllers = SparseArray<BaseController<*, *>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return supportedControllers[viewType].createViewHolder(parent)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {

//        val controller1 = Controller1(R.layout.item_text_one_row)
//        model[position].also { item->
//            controller1.bind(holder, item)
//        }

        model[position].also { item ->
            (item.controller as Controller1).bind(holder as ViewHolder1, item as BindableItem<TextModel, ViewHolder1>) }
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