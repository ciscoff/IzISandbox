package s.yarlykov.izisandbox.utils

import android.util.Log
import com.google.gson.Gson

fun logIt(message: String, stamp: Boolean = false, tag: String = "APP_TAG") {

    val sz = if (stamp) {
        message + ", time=${System.currentTimeMillis().toString(16).takeLast(4)}"
    } else message


    Log.d(tag, sz)
}

fun logJson(obj : Any, message : String = "") {
    val gson = Gson()
//    println("$message ${gson.toJson(obj)}")
    logIt("$message ${gson.toJson(obj)}")
}