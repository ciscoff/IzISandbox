package s.yarlykov.izisandbox.shimmer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import s.yarlykov.izisandbox.R

class ShimmerActivity : AppCompatActivity() {

    private lateinit var shimmer : ShimmerLayoutV2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shimmer)

        shimmer = findViewById(R.id.shimmer)

        layoutInflater.inflate(R.layout.item_stub_card, shimmer, true)
    }
}