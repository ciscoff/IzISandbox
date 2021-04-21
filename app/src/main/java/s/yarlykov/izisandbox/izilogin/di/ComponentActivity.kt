package s.yarlykov.izisandbox.izilogin.di

import dagger.Subcomponent
import s.yarlykov.izisandbox.izilogin.IziLoginActivity

@ScopePerActivity
@Subcomponent(modules = [ModuleActivity::class])
interface ComponentActivity {
    val componentAuthBuilder: ComponentFragmentAuth.Builder
    val componentBoot: ComponentFragmentBoot
    fun inject(consumer: IziLoginActivity)
}