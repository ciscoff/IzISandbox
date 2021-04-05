package s.yarlykov.izisandbox.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.os.persistableBundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_buttons_list.*
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.telegram.v2.ViewHolderX

class ButtonsListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buttons_list)

        buttonsList.apply {
            layoutManager = LinearLayoutManager(this@ButtonsListActivity)
            adapter = ButtonsAdapter()
        }
    }
}

class ButtonsAdapter : RecyclerView.Adapter<ButtonsAdapter.ButtonsHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonsHolder {
        return ButtonsHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_buttons_list,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ButtonsHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = 3

    class ButtonsHolder(view : View) : RecyclerView.ViewHolder(view) {

        fun bind() {
            if(adapterPosition == 0) {
                itemView.findViewById<Button>(R.id.button1).apply {
                    visibility = View.GONE
                }

                itemView.findViewById<Button>(R.id.button2).apply {
                    visibility = View.GONE
                }
            }




        }


    }

}