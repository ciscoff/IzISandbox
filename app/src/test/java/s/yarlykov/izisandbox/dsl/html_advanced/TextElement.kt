package s.yarlykov.izisandbox.dsl.html_advanced

/**
 * Это просто текст (с отступом, который задается через indent)
 */
class TextElement(private val text: String) : Element {
    override fun render(builder: StringBuilder, indent: String) {
        builder.append("$indent$text")
    }
}