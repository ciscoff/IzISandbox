package s.yarlykov.izisandbox.dsl.html_advanced

import org.junit.Test


/**
 * Пример chaining'а
 *
 * Здесь смысл такой
 *
 */



//class ScopeRoot {
//    private val tag = "root"
//
//    fun createLayer1(name : String) = ScopeLayer1(name)
//
//    fun ScopeLayer1.setAttributes(map : Map<String, String>): ScopeLayer1 {
//    }
//
//    fun ScopeLayer1.setContent(data : String): ScopeLayer1 {
//    }
//
//}
//
//class ScopeLayer1(private val name: String) {
//
//    fun content() = "<$name></$name>"
//
//}


/**
 * Для тестирование Extension Member функции
 */
class ScopeHolder {

    private fun twice(): String {
        val dbgPrefix =
            "${this::class.java.simpleName}::${object {}.javaClass.enclosingMethod?.name}"
        return "$dbgPrefix: Hi !"
    }

    /**
     * Функцию можно вызывать только там, где контекстом исполнения является ScopeHolder.
     * И такая функция имеет доступ к private элементам внешнего инстанса.
     */
    fun String.twice() {

        val dbgPrefix =
            "${this::class.java.simpleName}::${object {}.javaClass.enclosingMethod?.name}"

        println("$dbgPrefix: $this,$this; ${this@ScopeHolder.twice()}")
    }
}

/**
 * Пример показывает, что в обоих случаях в лямбде доступны только public элементы
 */
class LambdaScopes {

    class Person(val name: String, private val age: Int)


    private fun Person.print() {
        println("Extension function. Only public members allowed. name=$name")
    }

    private fun printPerson(printer: Person.() -> Unit) {
        Person("Nick", 35).printer()
    }

    private fun printScopeHolder(printer: ScopeHolder.() -> Unit) {
        ScopeHolder().printer()
    }

    @Test
    fun testingScopes() {

        Person("Anna", 11).print()

        printPerson {
            println("Lambda with Receiver. Only public members allowed too. name=$name")
        }

        /**
         * Два способа вызвать Extension Member
         */
        printScopeHolder {
            "Hello_1".twice()
        }

        ScopeHolder().apply {
            "Hello_2".twice()
        }
    }
}