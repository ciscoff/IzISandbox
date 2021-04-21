package s.yarlykov.izisandbox.izilogin.di

import dagger.Subcomponent
import s.yarlykov.izisandbox.izilogin.FragmentAuth

@ScopePerFragmentAuth
@Subcomponent(modules = [ModuleFragmentAuth::class])
interface ComponentFragmentAuth {

    fun inject(consumer : FragmentAuth)

    @Subcomponent.Builder
    interface Builder {
        fun addModule(module: ModuleFragmentAuth): Builder
        fun build(): ComponentFragmentAuth
    }
}