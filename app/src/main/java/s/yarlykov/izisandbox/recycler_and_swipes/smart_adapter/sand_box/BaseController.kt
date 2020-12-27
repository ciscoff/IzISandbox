package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.sand_box

open class BaseController<H : AbstractHolder, I : BaseItemA<H>> {

    lateinit var h : H
    lateinit var i : I
}