package s.yarlykov.izisandbox.time_line.v4

enum class SeverityMode {
    Client,
    Master
}

interface AttributesAccessorV4 {
    val mode : SeverityMode
}