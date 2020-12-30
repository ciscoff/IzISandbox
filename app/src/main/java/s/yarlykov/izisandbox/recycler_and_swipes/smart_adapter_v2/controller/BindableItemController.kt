package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.controller

import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.holder.BindableViewHolder
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.item.BindableItem

abstract class BindableItemController<T, H : BindableViewHolder<T>> :
    BaseItemController<H, BindableItem<T, H>> {

    /**
     * Привязать данные элемента модели, хранящиеся в контейнере item к holder'у
     */
    override fun bind(holder: H, item: BindableItem<T, H>) {
        bind(holder, item.data)
    }

    /**
     * Привязать данные item к holder'у
     */
    fun bind(holder: H, data: T) {
        holder.bind(data)
    }
}