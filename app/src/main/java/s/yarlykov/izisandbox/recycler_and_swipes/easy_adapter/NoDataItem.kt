package s.yarlykov.izisandbox.recycler_and_swipes.easy_adapter

import androidx.recyclerview.widget.RecyclerView

/**
 * Элемент без данных
 */
class NoDataItem<H : RecyclerView.ViewHolder>(itemController: NoDataItemController<H>) : BaseItem<H>(itemController as BaseItemController<H, BaseItem<H>>)
