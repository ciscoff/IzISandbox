package s.yarlykov.izisandbox.dsl.html_simple

import org.junit.Test

class DslHtmlTest {
    @Test
    fun createPage() {
        val html = createHtml()
            .table {
                tr {
                    td {
                        row()
                    }
                }
            }
        println(html)
    }
}