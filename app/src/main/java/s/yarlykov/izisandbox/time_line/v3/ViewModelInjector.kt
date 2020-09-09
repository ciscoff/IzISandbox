package s.yarlykov.izisandbox.time_line.v3

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import s.yarlykov.izisandbox.time_line.TimeLineActivity

/**
 * Этому классу будет делегировано выполнение методов интерфейса ViewModelAccessor
 * при его инстанциации в TimeSurfaceV3.
 */
class ViewModelInjector(val context: Context) : ViewModelAccessor {

    override val activity: AppCompatActivity by lazy {
        try {
            context as TimeLineActivity
        } catch (e: ClassCastException) {
            throw ClassCastException("")
        }
    }
    override val viewModel: TimeLineViewModelV3 =
        ViewModelProvider(activity).get(TimeLineViewModelV3::class.java)
}



