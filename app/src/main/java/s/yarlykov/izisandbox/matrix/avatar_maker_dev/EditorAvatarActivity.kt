package s.yarlykov.izisandbox.matrix.avatar_maker_dev

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import s.yarlykov.izisandbox.R

class EditorAvatarActivity : AppCompatActivity() {

    companion object {
        const val IMAGE_ID = R.drawable.m_4
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor_avatar)
    }
}