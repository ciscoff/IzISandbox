package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.controller

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.item.ItemBase
import kotlin.random.Random

/**
 * @param <H> type of ViewHolder
 * @param <I> type of Item
 */
abstract class ItemControllerBase<H : RecyclerView.ViewHolder, I : ItemBase<H>> {

    companion object {
        const val NO_ID = RecyclerView.NO_ID
    }

    private val viewTypeIdsMap = mutableMapOf<Class<ItemControllerBase<H, I>>, Int>()

    abstract fun bind(holder: H, item: I)

    abstract fun createViewHolder(recyclerView: ViewGroup): H

    val viewType: Int
        get() = typeHashCode()

    fun typeHashCode(): Int {
        var id = viewTypeIdsMap[javaClass]

        if (id == null) {
            id = Random.nextInt()
            var hasId = false

            viewTypeIdsMap.forEach { (_, v) ->
                if (id == v) {
                    hasId = true
                    return@forEach
                }
            }

            if (hasId) return typeHashCode()

            viewTypeIdsMap[javaClass] = id
        }
        return id
    }

}