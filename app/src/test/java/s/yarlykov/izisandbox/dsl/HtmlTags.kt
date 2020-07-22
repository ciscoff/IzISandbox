package s.yarlykov.izisandbox.dsl

fun createHtml() : HTML {
    return HTML()
}

class HTML : Tag{
    fun table(init : TABLE.() -> String ) : String {
        return TABLE().init()
    }
}

class TABLE : Tag {
    private val tagOpen = "<table>"
    private val tagClose = "</table>"
    fun tr(init : TR.() -> String) : String {
        return "$tagOpen${TR().init()}$tagClose"
    }
}

class TR : Tag {
    private val tagOpen = "<tr>"
    private val tagClose = "</tr>"

    fun td(init : TD.() -> String) : String {
        return "$tagOpen${TD().init()}$tagClose"
    }

}

class TD : Tag {
    private val tagOpen = "<td>"
    private val tagClose = "</td>"

    fun row() : String = "${tagOpen}Hello, World !${tagClose}"
}