package s.yarlykov.izisandbox.time_line.domain

enum class SeverityMode {
    Client,
    Master
}

interface AttributesAccessor {
    val mode : SeverityMode
    val severityMode : SeverityMode
}