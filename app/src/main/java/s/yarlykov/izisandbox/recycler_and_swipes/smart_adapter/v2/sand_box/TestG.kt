package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.sand_box

import java.lang.IllegalStateException


interface Group<T> {
    fun put(data: T)
    fun fetch(): T
}

fun <T> testG(group: Group<T>) {
    println(group::class.java.simpleName)
}

open class Level1 : Group<String> {
    override fun put(data: String) {
    }

    override fun fetch(): String {
        return "level_1"
    }
}

class Level2 : Level1() {
    override fun fetch(): String {
        return "level_2"
    }
}

/**
 * Дженерики между собой ИНВАРИАНТНЫ
 */

abstract class Holder
class RealHolder : Holder()
//---------------------------------

abstract class BaseUno<H : Holder>(val controller: BaseUnoController<H, BaseUno<H>>)
class RealUno<H : Holder>(c: ChildController<H, BaseUno<H>>) : BaseUno<H>(c)
//---------------------------------

// Наследование от одного ДЖЕНЕРИКА к следующему ДЖЕНЕРИКУ и к конечному инстансу. ОК.

abstract class BaseUnoController<H : Holder, U : BaseUno<H>> {
    abstract fun fetchH(): H
    abstract fun fetchU(): U
    abstract fun putH(h : H)
    abstract fun putU(h : U)
}

open class ChildController<H : Holder, U : BaseUno<H>> : BaseUnoController<H, U>() {

    lateinit var dataH: H
    lateinit var dataU: U

    override fun putH(h: H) {
    }

    override fun putU(h: U) {
    }

    override fun fetchH(): H {
        return dataH
    }

    override fun fetchU(): U {
        throw IllegalStateException("")
    }
}

class RealController : ChildController<RealHolder, RealUno<RealHolder>>()

/**
 * Ф-ция ждет базовый дженерик, но принимает его производный инстанс
 */
fun useController(c: BaseUnoController<*, *>) {
    c.fetchH()
}

/**
 * Цепочка наследования дженериков - это наследование с использование type parameters, то есть
 * наследование без указания реальных type arguments. В этом цепочка subtyping'а сохраняется.
 *
 * Инвариантность возникает только после применения type arguments. До этого момента все ОК.
 * Вот ещё пример ниже:
 */
open class GenericA<T : Any>(val data: T) {
    open fun log() {
        println(data::class.java.simpleName)
    }
}

class GenericB<T : Any>(data: T) : GenericA<T>(data) {
    override fun log() {
        println(data::class.java.simpleName)
    }
}

fun <T : Any> useGeneric(g: GenericA<T>) {
    g.log()
}

//---------------------------------------------

open class Izi<T>
open class Pizza<T> : Izi<T>()
class RealPizza : Pizza<Any>()

// Наследование 'Izi<A> <- Pizza<A>' ОК.
open class TwoGenerics<A : Any, Z : Izi<A>>
open class TwoGenericsMore<A : Any, Z : Pizza<A>> : TwoGenerics<A, Z>()

fun useTwoGenerics(two : TwoGenerics<Int, Pizza<Int>>) {
    // OK
}

/**
 * Если я меняю A на Number, то компилятор ждет, что вторым аргументом будет Pizza<Number>.
 * Однако я подставляю RealPizza, то есть Pizza<Any>, а Pizza<Any> не разу не наследник от
 * Pizza<Number>
 */
//class RealTwoGenerics : TwoGenericsMore<Number, RealPizza>() // error

fun main(args: Array<String>) {
    testG(Level2())

    useController(RealController())
    useGeneric(GenericB<String>(""))
}