package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.base

/**
 * Прототип всех элементов данных, с которыми может работать наш адаптер
 */
interface DataItem {

    /**
     * viewType для выбора ViewHolder'а
     */
    val viewType: Int
}