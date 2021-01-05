package s.yarlykov.izisandbox.recycler_and_swipes.decorator.app.controller

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import kotlinx.android.synthetic.main.item_time_stamp.view.*
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.controller.BindableItemController
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.holder.BindableViewHolder
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.item.BindableItem

class TimeStampController(@LayoutRes val layoutRes: Int) :
    BindableItemController<String, TimeStampController.Holder>() {

    override fun createViewHolder(parent: ViewGroup): Holder {
        return Holder(parent, layoutRes)
    }

    override fun bind(holder: Holder, item: BindableItem<String, Holder>) {
        holder.bind(item.data)
    }

    override fun viewType(): Int = layoutRes


    /**
     * Класс viewHolder
     */
    class Holder(parent: ViewGroup, layoutRes: Int) :
        BindableViewHolder<String>(parent, layoutRes), StickyHolder {

        override fun bind(data: String) {
            itemView.time_tv.text = data
        }
    }

}