package s.yarlykov.izisandbox.recycler_and_swipes.decorator.app.controller

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.controller.NoDataItemController
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.holder.BaseViewHolder
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.item.NoDataItem

class StubViewController(@LayoutRes val layoutId: Int) :
    NoDataItemController<StubViewController.NoDataViewHolder>() {

    override fun createViewHolder(parent: ViewGroup): NoDataViewHolder = NoDataViewHolder(parent)

    override fun bind(holder: NoDataViewHolder, item: NoDataItem<NoDataViewHolder>) {
        // nothing to do
    }

    override fun viewType(): Int = layoutId

    /**
     * Пустой viewHolder
     */
    inner class NoDataViewHolder(parent: ViewGroup) : BaseViewHolder(parent, layoutId)
}