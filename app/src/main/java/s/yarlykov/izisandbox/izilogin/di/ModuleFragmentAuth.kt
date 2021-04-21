package s.yarlykov.izisandbox.izilogin.di

import android.net.Uri
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import javax.inject.Named

/**
 * Модуль, которому требуется аргумент.
 */
@Module
class ModuleFragmentAuth(private val authUriConst: String) {

    @Provides
    @ScopePerFragmentAuth
    fun loginInUsers(): Flow<String> = flow {
        val names = listOf("Anna", "Maria", "Georgy")

        while (true) {
            names.forEach {
                emit(it)
                delay(5000)
            }
        }
    }

    @Provides
    @Named("auth")
    @ScopePerFragmentAuth
    fun authUri(baseUri: Uri): Uri = Uri.parse("$baseUri$authUriConst")
}