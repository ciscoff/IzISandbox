package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.controller

import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.item.NoDataItem
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.vh.ViewHolderBase

/**
 * Контроллер для элементов без данных
 */
abstract class NoDataItemController<H : ViewHolderBase> :
    ItemControllerBase<H, NoDataItem<H>>() {

    override fun <H, I> bind(holder: H, item: I) {
        // nothing to do. no data
    }
}