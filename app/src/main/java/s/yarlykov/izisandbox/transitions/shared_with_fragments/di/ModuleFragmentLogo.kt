package s.yarlykov.izisandbox.transitions.shared_with_fragments.di

import android.content.Context
import dagger.Module
import dagger.Provides

/**
 * Модуль получает Context из родительского компонента, которым является AppComponent
 */

@Module
class ModuleFragmentLogo(private val titleId: Int) {

    @Provides
    @ScopeFragmentLogo
    fun crazyTitle(context: Context): String =
        context.getString(titleId)
            .mapIndexed { i, ch ->
                if (i % 2 != 0) {
                    ch.toUpperCase()
                } else {
                    ch.toLowerCase()
                }
            }
            .joinToString("")
}