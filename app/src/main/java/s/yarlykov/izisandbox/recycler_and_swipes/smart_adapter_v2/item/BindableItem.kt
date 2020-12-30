package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.item

import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.controller.BaseItemController
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.controller.BindableItemController
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.holder.BindableViewHolder

/**
 * Базовый контейнер для элемента модели, которая ИМЕЕТ данные <T>
 */
@Suppress("UNCHECKED_CAST")
class BindableItem<T, H : BindableViewHolder<T>>(
    val data: T,
    controller: BindableItemController<T, H>
) : BaseItem<H>(controller as BaseItemController<H, BaseItem<H>>)