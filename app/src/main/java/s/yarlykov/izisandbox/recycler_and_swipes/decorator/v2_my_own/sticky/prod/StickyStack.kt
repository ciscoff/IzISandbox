package s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.sticky.prod

import android.graphics.Bitmap

class StickyStack : Stack<StickyStack.Element> {

    /**
     * Отдельный элемент стека
     */
    data class Element(override val id: Int, val bitmap: Bitmap) : Stack.Element

    private var items = ArrayList<Element>()

    override val size: Int
        get() = items.size

    override fun contains(other: Element): Boolean {
        return other.id in items.map { it.id }
    }

    override fun notContains(other: Element): Boolean {
        return !contains(other)
    }

    override fun push(other: Element) {
        items.add(other)
    }

    override fun peek(): Element? {
        return try {
            items.last()
        } catch (e: Exception) {
            null
        }
    }

    @ExperimentalStdlibApi
    override fun pop(): Element? {
        return try {
            items.removeLast()
        } catch (e: Exception) {
            null
        }
    }

    override fun popUpTo(id: Int): Element? {
        val i = items.map { it.id }.indexOf(id)

        return if (i > 0) {
            items.subList(i, items.size).clear()
            items.removeLast()
        } else {
            null
        }
    }

    override fun pushOnce(other: Element) {
        if (notContains(other)) {
            push(other)
        }
    }

    override fun clear() {
        items.clear()
    }

    override fun log(): String {
        return items.map { it.id }.reversed().toString()
    }

    override fun isEmpty(): Boolean = items.isEmpty()
    override fun isNotEmpty(): Boolean = items.isNotEmpty()
}