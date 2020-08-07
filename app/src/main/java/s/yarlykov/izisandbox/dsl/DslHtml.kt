package s.yarlykov.izisandbox.dsl

@DslMarker
annotation class HtmlTagMarker

interface Element {
    fun append(builder: StringBuilder, indent: String)
}

/**
 * Это просто текстовый контент, умная обертка строки.
 * Внутри тэга: <tag>text element</tag>
 */
class TextElement(val text: String) : Element {
    override fun append(builder: StringBuilder, indent: String) {
        builder.append("$indent$text\n")
    }
}

@HtmlTagMarker
abstract class Tag(val name: String) : Element {

    val children = arrayListOf<Element>()
    val attributes = hashMapOf<String, String>()

    override fun append(builder: StringBuilder, indent: String) {
        builder.append("$indent<$name${renderAttributes()}>\n")
        children.forEach { it.append(builder, "$indent$indent") }
        builder.append("$indent</$name>\n")
    }

    private fun renderAttributes(): String {
        val builder = StringBuilder()
        for ((attr, value) in attributes) {
            builder.append(" $attr=\"$value\"")
        }
        return builder.toString()
    }

    /**
     * Это функция-шаблон для создания вложенных элементов. Она будет "декорироваться"
     * конкретными реализациями.
     */
    protected fun <T : Element> nestedTag(tag: T, init: T.() -> Unit): T {
        tag.init()
        children.add(tag)
        return tag
    }

    override fun toString(): String {
        val builder = StringBuilder()
        append(builder, " ")
        return builder.toString()
    }
}

/**
 * Это любой тег, который может содержать текстовый контент в виде дочернего TextElement
 * <tag>text_element</tag>
 *
 * В любой такой инстанс можно добавить строку унарным плюсом
 */
abstract class TagWithText(name: String) : Tag(name) {

    operator fun String.unaryPlus() {
        children.add(TextElement(this))
    }
}

/**
 * Класс BodyTag ограничивает область создания отдельных тегов (B, P, H1, A) только внутри
 * другого BodyTag. То есть инстансы этих классов нельзя породить например в HTML,
 * но в тоже время B можно породить внутри Р, они оба BodyTag'и.
 */
abstract class BodyTag(name: String) : TagWithText(name) {
    fun b(init: B.() -> Unit) = nestedTag(B(), init)
    fun p(init: P.() -> Unit) = nestedTag(P(), init)
    fun h1(init: H1.() -> Unit) = nestedTag(H1(), init)
    fun a(href: String, init: A.() -> Unit) {
        val a = nestedTag(A(), init)
        a.href = href
    }
}

/**
 * Любой элемент может содержать внутри текст комментов. Поэтому HTML, HEAD и BODY
 * являются TagWithText.
 */
class HTML : TagWithText("html") {

    operator fun invoke(init: HTML.() -> Unit) {
        this.init()
    }

    fun head(init: HEAD.() -> Unit) = nestedTag(HEAD(), init)
    fun body(init: BODY.() -> Unit) = nestedTag(BODY(), init)
}

class HEAD : TagWithText("head") {
    fun title(init: TITLE.() -> Unit) = nestedTag(TITLE(), init)
}

/**
 *Текст заголовка добавляется унарным плюсом
 */
class TITLE : TagWithText("title")

class BODY : BodyTag("body")

class B : BodyTag("b")
class P : BodyTag("p")
class H1 : BodyTag("h1")

class A : BodyTag("a") {

    var href: String
        get() = attributes["href"]!!
        set(value) {
            attributes["href"] = value
        }
}


