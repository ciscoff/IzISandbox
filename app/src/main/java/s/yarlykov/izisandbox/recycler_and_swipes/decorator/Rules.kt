package s.yarlykov.izisandbox.recycler_and_swipes.decorator

object Rules {
    const val MIDDLE = 1
    const val END = 2

    fun checkMiddleRule(rule: Int): Boolean {
        return rule and MIDDLE != 0
    }

    fun checkEndRule(rule: Int): Boolean {
        return rule and END != 0
    }

    fun checkAllRule(rule: Int): Boolean {
        return rule and (MIDDLE or END) != 0
    }
}