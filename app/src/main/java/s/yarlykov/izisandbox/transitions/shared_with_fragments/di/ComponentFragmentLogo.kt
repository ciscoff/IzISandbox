package s.yarlykov.izisandbox.transitions.shared_with_fragments.di

import dagger.Subcomponent
import s.yarlykov.izisandbox.transitions.shared_with_fragments.FragmentLogo

@ScopeFragmentLogo
@Subcomponent(modules = [ModuleFragmentLogo::class])
interface ComponentFragmentLogo {

    fun inject(consumer: FragmentLogo)
    fun getTitle(): String

    @Subcomponent.Builder
    interface Builder {
        fun plus(module: ModuleFragmentLogo): Builder
        fun build(): ComponentFragmentLogo
    }
}