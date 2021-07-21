package s.yarlykov.izisandbox.ui.shimmer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.holder.BaseViewHolder

/**
 * @param layoutId - это id макета с корневым элементом [ru.mts.push.presentation.ui.ShimmerLayout]
 */
class StubViewHolder(parent: ViewGroup, @LayoutRes val layoutId: Int) :
    BaseViewHolder(parent, layoutId) {

    /**
     * При инициализации ViewHolder инфлейтим макет с декоративными элементоами
     * в контейнер [ru.mts.push.presentation.ui.ShimmerLayout]
     */
    init {
        LayoutInflater
            .from(parent.context)
            .inflate(R.layout.layout_stub_card_view, itemView as ViewGroup, true)
    }
}