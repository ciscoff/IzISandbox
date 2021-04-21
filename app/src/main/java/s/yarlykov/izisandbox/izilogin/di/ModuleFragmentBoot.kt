package s.yarlykov.izisandbox.izilogin.di

import android.net.Uri
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Named

@Module
class ModuleFragmentBoot {

    private val bootUriConst = "/boot"

    @Provides
    @ScopePerFragmentBoot
    fun bootProgress(): Flow<Int> = flow {

        while (true) {
            (0..100 step 5).forEach {
                emit(it)
                delay(200)
            }
        }
    }

    @Provides
    @Named("boot")
    @ScopePerFragmentBoot
    fun bootUri(baseUri: Uri): Uri = Uri.parse("$baseUri$bootUriConst")
}