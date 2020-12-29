package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.base.BaseViewHolder
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.base.DataItem

class TwoRowsViewHolder : BaseViewHolder {

    /**
     * Элементы для создания ViewHolder'а
     * - layoutId: id макета
     * - action: код для создания инстанса. Вызывается фабрикой ViewHolderFabric.
     */
    companion object Create {

        const val layoutId = R.layout.item_text_two_rows

        fun action(recyclerView: ViewGroup): TwoRowsViewHolder {
            return TwoRowsViewHolder(recyclerView, layoutId)
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