package s.yarlykov.izisandbox.izilogin.di

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

@Module
class ModuleB {

    @Provides
    fun flowNames(): Flow<String> {
        return listOf("Anna", "Maria", "Georgy").asFlow()
    }
}