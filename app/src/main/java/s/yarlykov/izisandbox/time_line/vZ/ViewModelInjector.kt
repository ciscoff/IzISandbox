package s.yarlykov.izisandbox.time_line.vZ

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import s.yarlykov.izisandbox.time_line.TimeLineActivity
import s.yarlykov.izisandbox.time_line.v4.TimeLineViewModelV4
import s.yarlykov.izisandbox.time_line.v4.ViewModelAccessorV4

/**
 * Этому классу будет делегировано выполнение методов интерфейса ViewModelAccessor
 * при его инстанциации в TimeSurfaceV3.
 */
class ViewModelInjector(val context: Context) :
    ViewModelAccessor {

    override val activity: AppCompatActivity by lazy {
        try {
            context as TimeLineActivity
        } catch (e: ClassCastException) {
            throw ClassCastException("")
        }
    }
    override val viewModel: TimeLineViewModel =
        ViewModelProvider(activity).get(TimeLineViewModel::class.java)
}



