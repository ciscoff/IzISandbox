package s.yarlykov.izisandbox.application

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import s.yarlykov.izisandbox.application.di.AppComponent
import s.yarlykov.izisandbox.application.di.DaggerAppComponent

class App : Application() {

    lateinit var appComponent : AppComponent

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)

        appComponent = DaggerAppComponent.create()
    }
}