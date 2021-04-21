package s.yarlykov.izisandbox.application.di

import dagger.Component
import s.yarlykov.izisandbox.izilogin.di.ComponentActivity
import s.yarlykov.izisandbox.izilogin.repo.AuthRepo
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun authRepo(): AuthRepo
    val componentActivity: ComponentActivity
}