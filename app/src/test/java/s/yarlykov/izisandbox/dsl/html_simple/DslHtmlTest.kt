package s.yarlykov.izisandbox.dsl

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