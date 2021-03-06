package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.adapter

import android.util.SparseArray
import android.view.ViewGroup
import androidx.core.util.set
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.controller.BaseController
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.holder.BaseViewHolder
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.holder.EventWrapper
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.model.SmartList
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.model.item.BaseItem

/**
 * Алгоритм работы следующий:
 * Адаптер использует в качестве модели SmartList Item'ов. Каждый Item имеет ссылку на свой
 * контроллер и опционально ссылку на 'data: T' (если данные имеются). На каждый viewType создается
 * отдельный контроллер. Зависимость такова: ControllerA -> layoutIdA -> ViewHolderA, то есть
 * layoutIdA - это viewType, а ViewHolderA знает только про иерархию внутри layoutIdA. Универсальные
 * холдеры под несколько layoutId не применяются.
 *
 * Для создания ViewHolder'а и binding'а адаптер использует контроллер Item'а.
 * - Создание холдера: контроллер инфлейтит view с помощью layoutId, создает и возвращает холдер.
 * - Binding: адаптер передает контроллеру созданный ранее холдер и ссылку на Item.
 */

class SmartAdapterV2 : RecyclerView.Adapter<BaseViewHolder>() {

    /**
     * Модель данных
     */
    private val model = SmartList()

    /**
     * EventBus в виде Subject'а для ретрансляции сообщений от ViewHolder'ов.
     */
    private val events = PublishSubject.create<EventWrapper<Any>>()
    val eventBus: Observable<EventWrapper<Any>> by lazy {
        events.hide()
    }

    /**
     * Список контроллеров для текущей модели данных
     */
    private val supportedControllers = SparseArray<BaseController</*H*/*, /*I*/*>>()

    /**
     * Создаем ViewHolder и привязываем его к шине данных.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {

        return supportedControllers[viewType].createViewHolder(parent).apply {
            eventsObservable.subscribe(events)
        }
    }

    /**
     * Короче вот этот ад с явным кастом и type projection позволил запуститься.
     */
    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        // Вот этот код не запуститься, потому что автоматическая type projection
        // блокирует.
//        model[position].let { item ->
//            item.controller.bind(holder, item)
//        }

        val baseItem = model[position] as BaseItem<BaseViewHolder>
        val controller =
            baseItem.controller as BaseController<in BaseViewHolder, in BaseItem<BaseViewHolder>>

        controller.bind(holder, baseItem)
    }

    override fun getItemViewType(position: Int): Int {
        return model[position].controller.viewType()
    }

    override fun getItemCount(): Int = model.size

    /**
     * View холдера @holder перешло в состояние Recycled
     */
    override fun onViewRecycled(holder: BaseViewHolder) {
        super.onViewRecycled(holder)
        holder.clear()
    }

    /**
     * Обновляем модель данных
     */
    fun updateModel(smartList: SmartList) {

        model.apply {
            clear()
            addAll(smartList)
        }

        // Обновить контроллеры
        updateControllers()
        notifyDataSetChanged()
    }

    /**
     * Обновляем модель контроллеров.
     * Модель данных состоит из Item'ов, каждый из которых содержит ссылку на свой контроллер.
     */
    private fun updateControllers() {
        supportedControllers.apply {
            clear()
            model.forEach { item -> this[item.controller.viewType()] = item.controller }
        }
    }
}