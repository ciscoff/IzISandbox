package s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.domain

enum class DoerRole(val roleId: Int) {
    Admin(1),
    Manager(2),
    Employee(3);

    // Для обратного пробразования Int в DoerRole
    companion object {
        val map: MutableMap<Int, DoerRole> = HashMap()

        init {
            for (i in values()) {
                map[i.roleId] = i
            }
        }

        fun fromInt(type: Int): DoerRole? {
            return map[type]
        }
    }
}