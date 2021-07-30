package s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.sticky.dev

interface StackV1 <T : Any > {
    val size : Int

    fun contains(other : T) : Boolean
    fun notContains(other : T) : Boolean
    fun pushOnce(other : T)
    fun push(other : T)
    fun peek() : T?
    fun pop() : T?
    fun isEmpty() : Boolean
    fun isNotEmpty() : Boolean
    fun clear()
}