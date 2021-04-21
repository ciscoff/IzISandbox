package s.yarlykov.izisandbox.izilogin.di

import android.net.Uri
import dagger.Module
import dagger.Provides

@Module
class ModuleActivity {

    private val baseUri = "https://auth.io"

    @Provides
    @ScopePerActivity
    fun baseUri(): Uri = Uri.parse("$baseUri/login")
}