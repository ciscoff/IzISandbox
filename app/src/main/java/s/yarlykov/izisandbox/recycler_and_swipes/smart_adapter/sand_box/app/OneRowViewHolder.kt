package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.sand_box.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.base.BaseViewHolder
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.base.DataItem

class OneRowViewHolder : BaseViewHolder {

    /**
     * Элементы для создания ViewHolder'а
     * - layoutId: id макета
     * - action: код для создания инстанса. Вызывается фабрикой ViewHolderFabric.
     */
    companion object Create {

        const val layoutId = R.layout.item_text_one_row

        fun action(recyclerView: ViewGroup) : OneRowViewHolder {
            return OneRowViewHolder(recyclerView, layoutId)
        }
    }

    constructor(recyclerView: ViewGroup, @LayoutRes layoutId: Int)
            : super(
        LayoutInflater.from(recyclerView.context).inflate(layoutId, recyclerView, false)
    )

    constructor(itemView: View) : super(itemView)

    override fun bind(item: DataItem) {
        // no data
    }
}