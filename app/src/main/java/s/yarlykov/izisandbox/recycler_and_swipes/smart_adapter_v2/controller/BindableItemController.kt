package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.controller

import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.holder.BindableViewHolder
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.item.BindableItem

abstract class BindableItemController<T, H : BindableViewHolder<T>> :
    BaseController<H, BindableItem<T, H>>() {

    /**
     * Привязать данные элемента модели, хранящиеся в контейнере item к holder'у
     */

    /**
     * Привязать данные item к holder'у
     */
    fun bind(holder: H, data: T) {
        holder.bind(data)
    }
}