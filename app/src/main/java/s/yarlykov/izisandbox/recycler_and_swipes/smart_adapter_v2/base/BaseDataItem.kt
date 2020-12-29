package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.base

open class BaseDataItem(private val clazz: Class<out BaseViewHolder>) : DataItem {

    override val viewType: Int
        get() = clazz.hashCode()
}