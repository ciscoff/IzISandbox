package s.yarlykov.izisandbox.generics

open class Animal(val name: String)

open class Cat(name: String) : Animal(name)
class BlackCat(name: String) : Cat(name)
class WhiteCat(name: String) : Cat(name)


open class Dog(name: String) : Animal(name)
class KindDog(name: String) : Dog(name)
class AngryDog(name: String) : Dog(name)

