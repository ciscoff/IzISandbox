package s.yarlykov.izisandbox.time_line.v4

import androidx.appcompat.app.AppCompatActivity
import s.yarlykov.izisandbox.time_line.v3.TimeLineViewModelV3

interface ViewModelAccessorV4 {
    val viewModel : TimeLineViewModelV4
    val activity : AppCompatActivity
}

