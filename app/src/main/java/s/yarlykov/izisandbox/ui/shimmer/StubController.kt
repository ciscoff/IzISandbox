package s.yarlykov.izisandbox.ui.shimmer

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.controller.NoDataItemController
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.model.item.NoDataItem
import java.util.stream.Collector

class StubController(@LayoutRes val layoutRes: Int) : NoDataItemController<StubViewHolder>() {

    override fun bind(holder: StubViewHolder, item: NoDataItem<StubViewHolder>) {
        // nothing to do
    }

    override fun viewType(): Int {
        return layoutRes
    }

    override fun createViewHolder(parent: ViewGroup): StubViewHolder {
        return StubViewHolder(parent, layoutRes)
    }
}