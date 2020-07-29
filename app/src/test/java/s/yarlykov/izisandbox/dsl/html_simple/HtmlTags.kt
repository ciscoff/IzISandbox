package s.yarlykov.izisandbox.dsl.html_simple

import s.yarlykov.izisandbox.dsl.Tag

fun createHtml() : HTML {
    return HTML()
}

class HTML : Tag("html"){
    fun table(init : TABLE.() -> String ) : String {
        return "<$name>${TABLE().init()}</$name>"
    }
}

class TABLE : Tag("table") {
    fun tr(init : TR.() -> String) : String {
        return "<$name>${TR().init()}</$name>"
    }
}

class TR : Tag("tr") {
    fun td(init : TD.() -> String) : String {
        return "<$name>${TD().init()}</$name>"
    }

}

class TD : Tag("td") {
    fun row() : String = "<$name>Hello, World !</$name>"
}