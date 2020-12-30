package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v1.fabric

import android.view.ViewGroup
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v1.base.BaseViewHolder

/**
 * Наследник DataItem регистрирует пары "viewType : ViewHolder::create".
 * Адаптер по viewType запрашивает функцию и с её помощью создает инстанс viewHolder'а.
 */
object ViewHolderFabric {

    private val db = mutableMapOf<Int, (ViewGroup) -> BaseViewHolder>()

    fun registerViewHolder(
        viewType: Int,
        creator: (ViewGroup) -> BaseViewHolder
    ) {
        db[viewType] = creator
    }

    fun withType(viewType : Int) : (ViewGroup) -> BaseViewHolder {
        return db[viewType] ?: throw IllegalArgumentException("Unknown viewType")
    }
}