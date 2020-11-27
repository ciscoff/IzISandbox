package s.yarlykov.izisandbox.places

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.android.synthetic.main.activity_places_auto_complete.*
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.utils.logIt

class PlacesAutoCompleteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_places_auto_complete)

        val key = "AIza" + getString(R.string.simple_id) + "BJcEKE"

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, key)
        }

        // ???
//        val placesClient  = Places.createClient(this)

        val autocompleteFragment =
            (supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment).apply {
                setPlaceFields(mutableListOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG))

                setOnPlaceSelectedListener(selectionListener)
            }
    }

    private val selectionListener = object : PlaceSelectionListener {
        override fun onPlaceSelected(p0: Place) {
            val result = "Place: ${p0.address}, LatLng=${p0.latLng}"
            placeResult.text = result
        }

        override fun onError(p0: Status) {
            logIt("PlaceSelectionListener: error $p0")
        }
    }
}