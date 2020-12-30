package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.sand_box

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.controller.BindableItemController
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.holder.BaseViewHolder
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.item.BaseItem
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.item.BindableItem

/**
 * Определяем конкретный ТИП на базе дженерик-КЛАССОВ, поэтому для всех type parameters
 * устанавливаем явные ТИПЫ, а именно TextModel и ViewHolder1.
 *
 */
class Controller2(@LayoutRes val layoutRes: Int) : BindableItemController<TextModel, ViewHolder2>() {

    private var viewType : Int = 0
    override fun bind(holder: ViewHolder2, item: BindableItem<TextModel, ViewHolder2>) {

    }

    override fun createViewHolder(parent: ViewGroup): ViewHolder2 {
        return ViewHolder2(parent, layoutRes)
    }


    override fun viewType(): Int = layoutRes
}