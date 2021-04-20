package s.yarlykov.izisandbox.izilogin.di

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

@Module
class ModuleC {

    @Provides
    fun flowNumbers(): Flow<Int> {
        return listOf(1, 50, 30, 0, 99, 50).asFlow()
    }
}