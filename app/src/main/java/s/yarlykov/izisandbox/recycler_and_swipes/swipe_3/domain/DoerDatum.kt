package s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.domain

class DoerDatum {
    val doerRole: Int = DoerRole.Admin.roleId
}

fun DoerDatum.role(): DoerRole {
    return DoerRole.fromInt(doerRole) ?: DoerRole.Employee
}