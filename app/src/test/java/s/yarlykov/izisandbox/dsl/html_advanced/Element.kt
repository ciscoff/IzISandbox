package s.yarlykov.izisandbox.dsl.html_advanced

interface Element {
    fun render(builder : StringBuilder, indent : String = "\t")
}