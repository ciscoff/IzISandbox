package s.yarlykov.izisandbox.transitions.shared_with_fragments.di

import dagger.Subcomponent
import s.yarlykov.izisandbox.transitions.shared_with_fragments.ActivityWithFragments

/**
 * Родительский компонент должен предосталять:
 * - либо инстанс дочернего сабкомпонента
 * - либо инстанс билдера дочернего сабкомпонента
 *
 * Если используется второй вариант, то аргументы конструктору модуля передаст
 * внешний код, который вызовет соответствующий метод билдера.
 *
 * Проблема имеет место, если у нас такая цепочка:
 *
 * AppComponent <-- SubComponentA <-- SubComponentB
 *
 * и оба Sub-компонента инстанциируются через билдеры. Если, например, в FragmentB нужно
 * получить SubComponentB, то придется делать так:
 *
 * appComponent
 *  .builderSubComponentA
 *  .plus(ModuleA(argA))
 *  .build()
 *    .builderSubComponentB
 *    .plus(ModuleB(argB))
 *    .build()
 *
 * То есть из FragmentB нужно 'идти' по всей цепочке, да ещё и аргумент argA где-то взять.
 * Видимо такие конфигурации - не есть хорошо )
 */
@ScopeTransitionActivity
@Subcomponent(modules = [ModuleTransitionActivity::class])
interface ComponentTransitionActivity {

    fun inject(consumer: ActivityWithFragments)

    @Subcomponent.Builder
    interface Builder {
        fun plus(module: ModuleTransitionActivity): Builder
        fun build(): ComponentTransitionActivity
    }
}