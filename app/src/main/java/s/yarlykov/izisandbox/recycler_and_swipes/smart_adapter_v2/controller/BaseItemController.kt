package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.controller

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.holder.BaseViewHolder
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.item.BaseItem

/**
 * Базовый класс контроллера. Его задача сформировать viwType, сгенерить ViewHolder и
 * сбиндить holder и элемент модели <T>. viewType формируется на базе двух значений:
 * - тип данных T
 * - layoutId для элемента списка
 *
 * Это позволит не только назначать различные холдеры для различных данных, но и
 * показывать однотипные данные в разных представляениях. Например часть элемента однотипного
 * списка вывести в одной layout, часть в другой и каждая со своим типом холдера.
 */
abstract class BaseItemController<H : BaseViewHolder, I : BaseItem<H>> {

    abstract fun createViewHolder(parent: ViewGroup): H
//    abstract fun setViewType(clazz: Class<Any>)

    /**
     * Адаптер вызывает этот метод и "знакомит" контроллер с его контейнером (Item).
     * Если у контейнера есть данные, то он передаст их в холдер через holder.bind(item.data)
     */
    abstract fun <H : BaseViewHolder, I : BaseItem<H>> bind(holder: H, item: I)
    abstract fun viewType(): Int
}