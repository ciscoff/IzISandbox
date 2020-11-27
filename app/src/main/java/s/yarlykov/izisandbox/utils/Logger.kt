package s.yarlykov.izisandbox.utils

import android.util.Log
import com.google.gson.Gson


fun logIt(message : String, tag : String = "APP_TAG") {
    Log.e(tag, message)
}

fun logJson(obj : Any, message : String = "") {
    val gson = Gson()
//    println("$message ${gson.toJson(obj)}")
    logIt("$message ${gson.toJson(obj)}")
}