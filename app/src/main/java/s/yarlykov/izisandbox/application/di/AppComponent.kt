package s.yarlykov.izisandbox.application.di

import dagger.Component
import s.yarlykov.izisandbox.izilogin.di.ComponentLoginActivity
import s.yarlykov.izisandbox.izilogin.repo.AuthRepo
import s.yarlykov.izisandbox.transitions.shared_with_fragments.di.ComponentFragmentLogo
import s.yarlykov.izisandbox.transitions.shared_with_fragments.di.ComponentTransitionActivity
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    @Component.Builder
    interface Builder {
        fun plus(module: AppModule): Builder
        fun build(): AppComponent
    }

    fun authRepo(): AuthRepo

    val componentLoginActivity: ComponentLoginActivity
    val builderComponentTransitionActivity: ComponentTransitionActivity.Builder
    val builderComponentFragmentLogo: ComponentFragmentLogo.Builder
}