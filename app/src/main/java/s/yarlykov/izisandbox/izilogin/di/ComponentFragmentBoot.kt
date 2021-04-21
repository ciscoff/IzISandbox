package s.yarlykov.izisandbox.izilogin.di

import android.net.Uri
import dagger.Subcomponent
import kotlinx.coroutines.flow.Flow

@ScopePerFragmentBoot
@Subcomponent(modules = [ModuleFragmentBoot::class])
interface ComponentFragmentBoot {
    val bootUri: Uri
    val bootProgress: Flow<Int>
}