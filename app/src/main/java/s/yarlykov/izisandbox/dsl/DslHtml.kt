package s.yarlykov.izisandbox.dsl

@DslMarker
annotation class HtmlTagMarker

interface Element {
    fun append (builder : StringBuilder, indent : String)
}

/**
 * Это просто текстовый контент.
 * Просто строка, например внутри тэга: <tag>text element</tag>
 */
class TextElement(val text : String) : Element {
    override fun append(builder: StringBuilder, indent: String) {
        builder.append("$indent$text")
    }
}

@HtmlTagMarker
abstract class Tag(val name : String) : Element {

    val children = arrayListOf<Element>()

    override fun append(builder: StringBuilder, indent: String) {
        TODO("Not yet implemented")
    }

    protected fun <T : Element> nestedTag(tag : T, init: T.() -> Unit) : T {
        tag.init()
        children.add(tag)
        return tag
    }
}

