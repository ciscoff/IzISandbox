package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v1.sand_box.kotlin

open class BaseController<H : AbstractHolder, I : BaseItemA<H>> {

    fun putHolder(holder : H) {}

    fun fetchItem() : I {
        return BaseItemA<H>(this) as I
    }

    lateinit var h : H
    lateinit var i : I
}