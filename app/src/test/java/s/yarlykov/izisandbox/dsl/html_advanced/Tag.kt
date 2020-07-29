package s.yarlykov.izisandbox.dsl.html_advanced


@HtmlTagMarker
open class Tag(val name: String) : Element {

    /**
     * У тэга могут быть вложенные тэги
     */
    protected val children = arrayListOf<Element>()

    /**
     * У тега могут быть атрибуты
     */
    protected val attributes = mutableMapOf<String, String>()

    /**
     * Функция иниициализирует вложенный тэг, а затем добавляет его в свой children.
     * Причем алгоритм инициализации будет предоставлен из вне.
     */
    protected fun <T : Element> initSubTag(subTag : T, init: T.() -> Unit) : T {
        subTag.init()
        children.add(subTag)
        return subTag
    }

    /**
     * Сгенерить строку-список пар атрибут="значение" атрибут="значение" ...
     */
    private fun renderAttributes(): String {
        val builder = StringBuilder()
        for ((attr, value) in attributes) {
            builder.append(" $attr=\"$value\"")
        }
        return builder.toString()
    }

    override fun toString(): String {
        val builder = StringBuilder()
        render(builder, "")
        return builder.toString()
    }

    /**
     * Распечатать себя с атрибутами и вложенные тэги с их атрибутами
     */
    override fun render(builder: StringBuilder, indent: String) {
        builder.append("$indent<$name${renderAttributes()}>\n")
        for (c in children) {
            c.render(builder, "$indent$indent")
        }
        builder.append("$indent</$name>\n")
    }
}