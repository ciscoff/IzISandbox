package s.yarlykov.izisandbox.generics

open class Animal(val name: String)

open class Cat(name: String) : Animal(name)
class BlackCat(name: String = "Black Cat") : Cat(name)
class WhiteCat(name: String = "White Cat") : Cat(name)

open class Dog(name: String) : Animal(name)
class KindDog(name: String = "Kind Dog") : Dog(name)
class AngryDog(name: String = "Angry Dog") : Dog(name)

