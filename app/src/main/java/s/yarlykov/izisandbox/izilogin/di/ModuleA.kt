package s.yarlykov.izisandbox.izilogin.di

import dagger.Module
import dagger.Provides

@Module
class ModuleA(val title : String = "ModuleA") {

    @Provides
    fun upperTitle() : String = title.toUpperCase()
}