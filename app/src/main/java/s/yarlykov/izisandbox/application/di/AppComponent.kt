package s.yarlykov.izisandbox.application.di

import dagger.Component
import s.yarlykov.izisandbox.izilogin.repo.AuthRepo

@Component
interface AppComponent {
    fun authRepo(): AuthRepo
}