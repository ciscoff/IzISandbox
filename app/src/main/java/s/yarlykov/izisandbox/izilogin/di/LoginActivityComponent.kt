package s.yarlykov.izisandbox.izilogin.di

import dagger.Subcomponent

@ScopePerActivity
@Subcomponent
interface LoginActivityComponent {

    @Subcomponent.Builder
    interface Builder {
        fun plusModuleA(module: ModuleActivity): Builder
        fun build(): LoginActivityComponent
    }
}