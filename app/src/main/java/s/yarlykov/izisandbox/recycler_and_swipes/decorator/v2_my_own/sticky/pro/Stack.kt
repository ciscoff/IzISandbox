package s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.sticky.pro

interface Stack<T : Stack.Element> {

    interface Element {
        val id: Int
    }

    val size: Int

    fun contains(other: T): Boolean
    fun notContains(other: T): Boolean
    fun pushOnce(other: T)
    fun push(other: T)
    fun peek(): T?
    fun pop(): T?
    fun popUpTo(id: Int): T?
    fun isEmpty(): Boolean
    fun isNotEmpty(): Boolean
    fun clear()
    fun log(): String
}