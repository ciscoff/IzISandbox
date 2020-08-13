package s.yarlykov.izisandbox.collections

import org.junit.Test

class TestIntersect {

     private val map = mapOf(
        1 to setOf(10, 11, 15),
        2 to setOf(11, 14, 10),
        3 to setOf(17, 21, 11, 10),
        4 to setOf(11, 16, 22, 10)
    )


    @Test
    fun testIntersect() {

        var result = mutableSetOf<Int>()

            for((index, set) in map.values.withIndex()) {
                if(index == 0) result.addAll(set)
                result = result.intersect(set).toMutableSet()
            }

        assert(result.size == 2)
        assert(result.size == 2 && result.contains(11) && result.contains(10))
    }
}