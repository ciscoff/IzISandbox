package s.yarlykov.izisandbox.time_line.vZ

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import s.yarlykov.izisandbox.time_line.TimeLineActivityEdu
import s.yarlykov.izisandbox.time_line.TimeLineActivityPro

/**
 * Этому классу будет делегировано выполнение методов интерфейса ViewModelAccessor
 * при его инстанциации в TimeSurfaceV3.
 */
class ViewModelInjectorPro(val context: Context) :
    ViewModelAccessorPro {

    override val activity: AppCompatActivity by lazy {
        try {
            context as TimeLineActivityPro
        } catch (e: ClassCastException) {
            throw ClassCastException("")
        }
    }
    override val viewModel: TimeLineViewModel =
        ViewModelProvider(activity).get(TimeLineViewModel::class.java)
}



