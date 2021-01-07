package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.sand_box

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.controller.BindableItemController
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.model.item.BindableItem

/**
 * Определяем конкретный ТИП на базе дженерик-КЛАССОВ, поэтому для всех type parameters
 * устанавливаем явные ТИПЫ, а именно TextModel и ViewHolder1.
 *
 * В данном случае создаем контроллер, который будет генерить
 */
class Controller1(@LayoutRes val layoutRes: Int) : BindableItemController<TextModel, ViewHolder1>() {

    override fun bind(holder: ViewHolder1, item: BindableItem<TextModel, ViewHolder1>) {
        bind(holder, item.data)
    }

    override fun createViewHolder(parent: ViewGroup): ViewHolder1 {
        return ViewHolder1(parent, layoutRes)
    }

    override fun viewType(): Int = layoutRes
}