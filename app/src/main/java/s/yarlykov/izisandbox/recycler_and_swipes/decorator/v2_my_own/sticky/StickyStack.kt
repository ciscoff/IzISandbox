package s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.sticky

import android.graphics.Bitmap

class StickyStack : ArrayList<Bitmap>() {

    fun containsOther(other : Bitmap) : Boolean {
        forEach {
            if(it.sameAs(other)) return true
        }

        return false
    }

    fun doesNotContain(other : Bitmap) : Boolean {
        return !containsOther(other)
    }

    fun pushOnce(other : Bitmap) {
        if(doesNotContain(other)) {
            add(other)
        }
    }
}