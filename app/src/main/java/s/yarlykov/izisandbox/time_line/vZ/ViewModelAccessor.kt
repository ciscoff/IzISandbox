package s.yarlykov.izisandbox.time_line.vZ

import androidx.appcompat.app.AppCompatActivity
import s.yarlykov.izisandbox.time_line.v4.TimeLineViewModelV4

interface ViewModelAccessor {
    val viewModel : TimeLineViewModel
    val activity : AppCompatActivity
}

