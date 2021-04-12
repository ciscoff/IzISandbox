package s.yarlykov.izisandbox.generics

open class Animal(val name: String)

open class Cat(name: String) : Animal(name)
class Siam(name: String = "Siam") : Cat(name)
class Pers(name: String = "Pers") : Cat(name)

open class Dog(name: String) : Animal(name)
class Taxa(name: String = "Taxa") : Dog(name)
class Beagle(name: String = "Beagle") : Dog(name)

