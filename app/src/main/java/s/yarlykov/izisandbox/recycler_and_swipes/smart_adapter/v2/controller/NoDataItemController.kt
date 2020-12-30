package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.controller

import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.holder.BaseViewHolder
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.item.NoDataItem

abstract class NoDataItemController<H : BaseViewHolder> : BaseController<H, NoDataItem<H>>()