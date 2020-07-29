package s.yarlykov.izisandbox.dsl.html_advanced

abstract class TagWithText(name: String) : Tag(name) {

    /**
     * Обрати внимание. Это функция расширения на строке. То есть внутри функции
     * this - это сама строка, а не инстанс TagWithText. Однако вызвать
     * данную функцию можно только в scope инстанса TagWithText. Не на нем самом,
     * а в том месте где он является this.
     */
    operator fun String.unaryPlus() {
        children.add(TextElement(this))
    }
}