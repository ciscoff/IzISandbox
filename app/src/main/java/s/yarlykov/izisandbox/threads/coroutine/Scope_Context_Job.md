## Задача: Описать интерфейс коллекции типа Map, а также типы ключа и элемента этой коллекции.

#### Элементами коллекции могут быть только такие же коллекции и каждый элемент должен содержать
#### public поле со своим ключем.

```kotlin
interface CoContext {                   // Интерфейс коллекции.
    interface Element : CoContext {     // Элемент коллекции, который тоже коллекция.
        val key: Key<*>                 // Public поле ключа в элементе.
    }

    interface Key<E : Element>      // Ключ коллекции. Один ключ для отдельного ТИПА элемента.

    // todo далее прототипы таких методов коллекции как get, plus, minus и т.д.
}
```


## Прототип элемента коллекции

```kotlin
interface Job : CoContext.Element {
    companion object Key : CoContext.Key<Job> // Это ключ для Job и всех его подтипов (CoCoroutine)
}
```


## Имплементация элемента коллекции

```kotlin 
class CoCoroutine : Job {
    override val key: CoContext.Key<*> = Job
}
```


## Имплементация коллекции

```kotlin
class CoContextImpl : CoContext {
    private lateinit var internalStore: Map<CoContext.Key<*>, CoContext.Element>

    /**
     * Пример получения элемента по ключу CoContext.Key<Job>
     *     
     * NOTE: на 'companion object' класса можно ссылаться по имени класса.
     * То есть выражение 'internalStore[Job]' вернет нам элемент Job
     */
     
    fun fetchJob() = internalStore[Job]
}
```