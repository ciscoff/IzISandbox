package s.yarlykov.izisandbox.dialogs

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit


class InputObservable {

    companion object {

        fun fromView(view: EditText): Observable<String> {

            return Observable.create<String> { emitter ->

                view.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                    }

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        emitter.onNext(s.toString())
                    }
                })
            }
                .debounce(500, TimeUnit.MILLISECONDS)
                .filter { it.length > 5 }
                .switchMap { text ->
                    Observable.fromCallable {
                        text.toLowerCase(Locale.getDefault()).trim()
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }
}