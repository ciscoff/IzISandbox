package s.yarlykov.izisandbox.application.di

import android.content.Context
import dagger.Module
import dagger.Provides
import s.yarlykov.izisandbox.izilogin.repo.AuthRepo
import s.yarlykov.izisandbox.izilogin.repo.AuthRepoImpl
import javax.inject.Singleton

@Module
class AppModule(@get:Provides val context: Context) {

    @Singleton
    @Provides
    fun authRepo(): AuthRepo = AuthRepoImpl()
}