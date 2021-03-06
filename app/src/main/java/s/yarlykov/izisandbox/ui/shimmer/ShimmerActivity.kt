package s.yarlykov.izisandbox.ui.shimmer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.ui.shimmer.arch.ShimmerLayoutV2

class ShimmerActivity : AppCompatActivity() {

    private lateinit var shimmer : ShimmerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shimmer)

        shimmer = findViewById(R.id.shimmer)

        layoutInflater.inflate(R.layout.item_stub_card, shimmer, true)
    }
}