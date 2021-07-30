package s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.sticky

import android.graphics.Bitmap

class StickyStackV1 : StackV1<Bitmap> {

    private val items = ArrayList<Bitmap>()

    override val size: Int
        get() = items.size

    override fun contains(other: Bitmap): Boolean {
        items.forEach {
            if (it.sameAs(other)) return true
        }
        return false
    }

    override fun notContains(other: Bitmap): Boolean {
        return !contains(other)
    }

    override fun push(other: Bitmap) {
        items.add(other)
    }

    override fun peek(): Bitmap? {
        return try {
            items.last()
        } catch (e: Exception) {
            null
        }
    }

    @ExperimentalStdlibApi
    override fun pop(): Bitmap? {
        return try {
            items.removeLast()
        } catch (e: Exception) {
            null
        }
    }

    override fun pushOnce(other: Bitmap) {
        if (notContains(other)) {
            push(other)
        }
    }

    override fun clear() {
        items.clear()
    }

    override fun isEmpty(): Boolean = items.isEmpty()
    override fun isNotEmpty(): Boolean = items.isNotEmpty()
}