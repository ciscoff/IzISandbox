package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.sand_box

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.controller.BindableItemController
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.holder.BaseViewHolder
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.holder.BindableViewHolder
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.item.BaseItem
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.item.BindableItem

/**
 * Определяем конкретный ТИП на базе дженерик-КЛАССОВ, поэтому для всех type parameters
 * устанавливаем явные ТИПЫ, а именно TextModel и ViewHolder1.
 *
 * В данном случае создаем контроллер, который будет генерить
 */
class Controller1(@LayoutRes val layoutRes: Int) : BindableItemController<TextModel, ViewHolder1>() {

    private var viewType : Int = 0

//    override fun <H, I> bind(holder: H, item: I) {
//        TODO("Not yet implemented")
//    }

    override fun <H : BaseViewHolder, I : BaseItem<H>> bind(holder: H, item: I) {
    }

    override fun createViewHolder(parent: ViewGroup): ViewHolder1 {
        return ViewHolder1(parent, layoutRes)
    }

    override fun setViewType(clazz: Class<Any>) {
        viewType = clazz.hashCode() + layoutRes
    }

    override fun viewType(): Int = layoutRes
}