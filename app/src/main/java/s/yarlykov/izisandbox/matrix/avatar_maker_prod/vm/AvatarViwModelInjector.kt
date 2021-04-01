package s.yarlykov.izisandbox.matrix.avatar_maker_prod.vm

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

class AvatarViwModelInjector(val context: Context) : AvatarViewModelAccessor {

    override val activity: AppCompatActivity by lazy {
        try {
            context as AppCompatActivity
        } catch (e: ClassCastException) {
            throw ClassCastException("")
        }
    }

    override val viewModel: AvatarViewModel =
        ViewModelProvider(activity).get(
            AvatarViewModel::class.java
        )
}