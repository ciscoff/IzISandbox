package s.yarlykov.izisandbox.time_line.v4

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import s.yarlykov.izisandbox.time_line.TimeLineActivity

/**
 * Этому классу будет делегировано выполнение методов интерфейса ViewModelAccessor
 * при его инстанциации в TimeSurfaceV3.
 */
class ViewModelInjectorV4(val context: Context) :
    ViewModelAccessorV4 {

    override val activity: AppCompatActivity by lazy {
        try {
            context as TimeLineActivity
        } catch (e: ClassCastException) {
            throw ClassCastException("")
        }
    }
    override val viewModel: TimeLineViewModelV4 =
        ViewModelProvider(activity).get(TimeLineViewModelV4::class.java)
}



