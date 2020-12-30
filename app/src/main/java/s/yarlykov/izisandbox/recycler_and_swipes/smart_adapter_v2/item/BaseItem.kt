package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.item

import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.controller.BaseItemController

/**
 * Базовый контейнер для элемента модели. Это не элемент модели, а именно контейнер.
 * Он ничего не знает о том как будет отображаться его элемент на экране.
 * Всю работу по отображению он делегирует контроллеру.
 */
open class BaseItem<H : RecyclerView.ViewHolder>(val controller: BaseItemController<H, BaseItem<H>>)