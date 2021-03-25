package s.yarlykov.izisandbox.matrix.avatar_maker_prod

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.matrix.avatar_maker_prod.vm.AvatarViewModel

class FunnyAvatarActivity : AppCompatActivity() {

    /**
     * В layout'е активити найти элемент <fragment> (или его производный)
     * с атрибутом 'app:navGraph'. Затем заинфлейтить этот граф из XML в NavGraph и
     * вернуть инстанс контроллера этого графа в виде NavController.
     */
    private val navController by lazy { findNavController(R.id.nav_avatar) }
    private lateinit var viewModel: AvatarViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_funny_avatar)

        viewModel = ViewModelProvider(
            navController.getViewModelStoreOwner(R.id.nav_avatar_graph)
        ).get(AvatarViewModel::class.java)
    }
}