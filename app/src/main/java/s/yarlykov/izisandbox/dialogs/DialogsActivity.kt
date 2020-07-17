package s.yarlykov.izisandbox.dialogs

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.createRegistrationFormDialog

class DialogsActivity : AppCompatActivity() {

    private lateinit var buttonCreateClient : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialogs)

        findViews()
        initViews()
    }

    private fun findViews() {
        buttonCreateClient = findViewById(R.id.btn_create_client)
    }

    private fun initViews() {
        buttonCreateClient.setOnClickListener {
            createDialog()
        }
    }

    private fun createDialog() {
        RegistrationDialog(this, layoutInflater).show()

//        this.createRegistrationFormDialog()
    }
}