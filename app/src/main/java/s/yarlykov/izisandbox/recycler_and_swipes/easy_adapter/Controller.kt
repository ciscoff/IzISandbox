package s.yarlykov.izisandbox.recycler_and_swipes.easy_adapter

import android.view.ViewGroup
import androidx.annotation.LayoutRes

class Controller(@LayoutRes val layoutRes: Int) : NoDataItemController<Controller.Holder>() {

    override fun viewType(): Int {
        return layoutRes
    }

    override fun createViewHolder(parent: ViewGroup): Holder = Holder(parent, layoutRes)

    class Holder(parent: ViewGroup, layoutRes: Int) : BaseViewHolder(layoutRes, parent)
}