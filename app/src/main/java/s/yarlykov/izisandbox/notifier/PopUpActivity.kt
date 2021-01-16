package s.yarlykov.izisandbox.notifier

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.RemoteInput
import kotlinx.android.synthetic.main.activity_pop_up.*
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.utils.logIt

class PopUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pop_up)
        textReply.text = getRemoteInput()

        logIt("PopUpActivity::onCreate")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        textReply.text = getRemoteInput()

        logIt("PopUpActivity::onNewIntent")
    }

    private fun getRemoteInput(): String? {
        val keyReply = getString(R.string.key_text_reply)
        return RemoteInput.getResultsFromIntent(intent)?.getCharSequence(keyReply)?.toString()
    }
}