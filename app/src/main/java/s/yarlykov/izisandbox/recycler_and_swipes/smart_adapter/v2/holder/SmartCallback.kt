package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.holder

fun interface SmartCallback <T : Any?> {
    fun call(arg : T)
}