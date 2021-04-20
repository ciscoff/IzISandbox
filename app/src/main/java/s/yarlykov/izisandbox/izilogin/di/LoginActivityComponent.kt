package s.yarlykov.izisandbox.izilogin.di

import dagger.Subcomponent

@ScopePerActivity
@Subcomponent
class LoginActivityComponent {

    @Subcomponent.Builder
    interface Builder {
        fun plusModuleA(module: ModuleA): Builder
        fun build(): LoginActivityComponent
    }
}