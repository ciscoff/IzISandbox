package s.yarlykov.izisandbox.matrix.avatar_maker_prod.vm

import androidx.appcompat.app.AppCompatActivity

interface AvatarViewModelAccessor {
    val viewModel : AvatarViewModel
    val activity : AppCompatActivity
}