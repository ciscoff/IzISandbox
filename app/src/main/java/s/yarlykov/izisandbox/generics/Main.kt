package s.yarlykov.izisandbox.generics

/**
 * Базовый Generic - это просто болванка, заготовка.
 * Он НЕ является ТИПОМ. Он является КЛАССОМ.
 *
 * T - это 'type parameter'
 *
 * parameter - это 'классификатор'
 * argument - конкрентное значение
 */
interface Group<T> {
    fun insert(t: T): Unit
    fun fetch(): T
}

/**
 * Для того, чтобы превратить класс в ТИП нужно вместо 'type parameter' указать 'type argument',
 * то есть конкретный тип вместо классификатора. Один из способов это сделать - type projection:
 *
 * NOTE: 'in Dog' - это уже КОНКРЕТНЫЙ ТИП, благодара которому абстракстная болванка
 * становится вот такого конкретного вида (это вытоматически сгенеренный код)
 */

// TODO Пример 1.
/*
class GroupDogIn : Group<in Dog> {
    override fun insert(t: Dog) {}

    override fun fetch(): Any? {
        return null
    }
}
*/

// TODO Пример 2.
/*
class GroupDogOut : Group<out Dog> {
    override fun insert(t: Nothing) {
    }

    override fun fetch(): Dog {
        return Dog("nora")
    }
}*/

/**
 * И хотя Kotlin запрещает использовать type projection в качестве аргумента супертипа
 * при декларации производных классов (пример выше), но не запрещает type projection для
 * аргументов функций:
 *
 * NOTE: То есть две функции работают фактически с РАЗНЫМИ ТИПАМИ, хотя вроде бы у обеих
 * аргументом является Group<Dog>. Но
 */

val myDogs = object : Group<Dog> {
    override fun insert(t: Dog) {
    }

    override fun fetch(): Dog {
        return Dog("Hunter")
    }
}


// TODO Здесь компилятор подставил тип из примера 1.
fun putDogs(dogs: Group<in Dog>) {
    dogs.insert(Dog("nora"))
}

// TODO Здесь компилятор подставил тип из примера 2.
fun getDogs(group: Group<out Dog>): Dog {
    return group.fetch()
}


fun main() {
    putDogs(myDogs)
    getDogs(myDogs)

    val animal = object : Group<Animal> {
        override fun insert(t: Animal) {
            TODO("Not yet implemented")
        }

        override fun fetch(): Animal {
            TODO("Not yet implemented")
        }
    }

    val dog = object : Group<Dog> {
        override fun insert(t: Dog) {
            TODO("Not yet implemented")
        }

        override fun fetch(): Dog {
            TODO("Not yet implemented")
        }
    }

    val beagle = object : Group<Beagle> {
        override fun insert(t: Beagle) {
        }

        override fun fetch(): Beagle = Beagle()
    }

    val taxa = object : Group<Taxa> {
        override fun insert(t: Taxa) {
        }

        override fun fetch(): Taxa = Taxa()
    }

    putDogs(dog)
    getDogs(dog)

    getDogs(dog)
    getDogs(beagle)
    //getDogs(animal)

    putDogs(dog)
    putDogs(animal)
    //putDogs(beagle)
}