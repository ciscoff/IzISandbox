package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.item

import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.controller.BaseItemController
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.holder.BaseViewHolder

/**
 * Базовый контейнер для элемента модели. Это не элемент модели, а именно контейнер.
 * Он ничего не знает о том как будет отображаться его элемент на экране.
 * Всю работу по отображению он делегирует контроллеру.
 */
abstract class BaseItem<H : BaseViewHolder>(val controller: BaseItemController<H, BaseItem<H>>)