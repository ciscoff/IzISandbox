package s.yarlykov.izisandbox.izilogin.di

import dagger.Subcomponent
import s.yarlykov.izisandbox.izilogin.IziLoginActivity

/**
 * Здесь реализована "прямая" зависимость когда сабкомпоненты получают от своего
 * родительского компонента и от своих модулей. Модулю родительского компонента
 * ничего не требуется от модулей сабкомпонентов.
 *
 * ComponentFragmentBoot инстанциируется без билдера, а ComponentFragmentAuth через
 * билдер с передачей аргумента в конструктор модуля ModuleFragmentAuth.
 */

@ScopePerActivity
@Subcomponent(modules = [ModuleActivity::class])
interface ComponentLoginActivity {
    val componentAuthBuilder: ComponentFragmentAuth.Builder
    val componentBoot: ComponentFragmentBoot
    fun inject(consumer: IziLoginActivity)
}