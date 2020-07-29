package s.yarlykov.izisandbox.dsl.chaining

/**
 * Пример chaining'a с использованием Member Extensions.
 *
 * Итак, класс Garage() работает как template и содержит методы для создания инстансов разных
 * типов машин, а также имеет Member Extensions, которые выполняются на этих машинах.
 * Мы наследуемся от Garage и соответственно получаем правильный контекст для вызова методов
 * placeInBox, addFuel и пр.
 *
 * Это extension функции определенные внутри класса Garage, то есть вызывать их можно только
 * в контексте его инстанса. А он у нас есть, мы же внутри Garage. Что это нам дает ?
 *
 * Мы получаем возможность внутри некоторого контейнера создавать вложенные сущности
 * и декларативно инициализировать их чейнингом. Кроме того методы инициализации
 * доступны только внутри контейнера, т.к. они его Member Extensions.
 *
 */

object GarageMitino : Garage() {

    private val my = truckCar("Caddy").placeInBox(1).addFuel(60)
        .putInGarage(this@GarageMitino::class.java.simpleName)
    private val ira = familyCar("Soul").placeInBox(2).addFuel(50)
        .putInGarage(this@GarageMitino::class.java.simpleName)

    override fun toString(): String {
        return "$my\n$ira\n"
    }
}

object GarageParent : Garage() {
    private val papa = familyCar("Cretta").placeInBox(3).addFuel(50)
        .putInGarage(this@GarageParent::class.java.simpleName)

    override fun toString(): String {
        return "$papa\n"
    }
}