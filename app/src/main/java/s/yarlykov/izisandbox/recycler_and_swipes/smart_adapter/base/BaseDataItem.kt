package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.base

/**
 * Недостаток в том, что мы узнаем viewType только после создания инстанса,
 * а может понадобиться сделать это раньше, чтобы, например, настроить декораторы
 * для разных viewType.
 */
abstract class BaseDataItem : DataItem {

    override val viewType: Int
        get() = this::class.java.viewType
}

/**
 * Но она решается вот так:
 */
val <I : Class<out BaseDataItem>> I.viewType: Int
    get() = this.hashCode()