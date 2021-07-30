package s.yarlykov.izisandbox.transitions.shared_with_fragments.di

import dagger.Module
import dagger.Provides
import s.yarlykov.izisandbox.transitions.shared_with_fragments.TitleWrapper

/**
 * Установить зависимость от билдера ComponentFragmentLogo.Builder
 *
 * Данный модуль зависит от данных, предоставляемых ЧУЖИМ сабкомпонентом. Модуль
 * может воспользоваться билдером этого сабкомпонентом, создать инстанс и явно (!)
 * запросить у него данные.
 */

// TODO Разобраться. В даггере v2.27 это работало. Сменил на v2.35.1 и ошибки пошли. Закоментил.

@Module/*(subcomponents = [ComponentFragmentLogo::class])*/
class ModuleTransitionActivity(private val titleId: Int) {

    @Provides
    fun titleWrapper(subComponentBuilder: ComponentFragmentLogo.Builder): TitleWrapper {

        val subComponent = subComponentBuilder
            .plus(ModuleFragmentLogo(titleId))
            .build()

        return TitleWrapper(subComponent.getTitle())
    }
}