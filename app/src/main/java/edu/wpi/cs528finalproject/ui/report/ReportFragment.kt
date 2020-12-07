package edu.wpi.cs528finalproject.ui.report

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues.TAG
import android.location.Location
import java.time.LocalDateTime
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.maps.android.SphericalUtil
import edu.wpi.cs528finalproject.LocationChangedListener
import edu.wpi.cs528finalproject.NavigationActivity
import edu.wpi.cs528finalproject.R


class ReportFragment : Fragment() {

    private lateinit var reportViewModel: ReportViewModel
    private lateinit var database: DatabaseReference

    private var selectedPlace: Place? = null
    private var dateDialog: DatePickerDialog? = null
    private var timeDialog: TimePickerDialog? = null

    private var currentLocation: Location? = null
    private val biasRadius = 5000.0

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        reportViewModel =
            ViewModelProvider(this).get(ReportViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_report, container, false)
//        val textView: TextView = root.findViewById(R.id.text_notifications)
//        notificationsViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })

        (requireActivity() as NavigationActivity).addOnLocationChangedListener(object :
            LocationChangedListener {
            override fun onLocationChanged(location: Location?) {
                currentLocation = location
            }
        })

        val locationAutocompleteFragment =
            childFragmentManager.findFragmentById(R.id.reportLocation)
                    as AutocompleteSupportFragment

        locationAutocompleteFragment.setHint(getString(R.string.LocationHint))
        locationAutocompleteFragment.setPlaceFields(listOf(Place.Field.LAT_LNG))
        locationAutocompleteFragment.setTypeFilter(TypeFilter.ESTABLISHMENT)

        val tempLoc = currentLocation
        if (tempLoc != null) {
            val currentLocationLatLng = LatLng(tempLoc.latitude, tempLoc.longitude)
            val bounds = LatLngBounds.Builder().
            include(SphericalUtil.computeOffset(currentLocationLatLng, biasRadius, 0.0)).
            include(SphericalUtil.computeOffset(currentLocationLatLng, biasRadius, 90.0)).
            include(SphericalUtil.computeOffset(currentLocationLatLng, biasRadius, 180.0)).
            include(SphericalUtil.computeOffset(currentLocationLatLng, biasRadius, 270.0)).
            build()
            locationAutocompleteFragment.setLocationBias(RectangularBounds.newInstance(bounds))
        }

        locationAutocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                selectedPlace = place
                Log.i(TAG, "Place: ${place.latLng}")
            }

            override fun onError(status: Status) {
                Log.i(TAG, "An error occurred: $status")
            }
        })



        val currentDateTime = LocalDateTime.now()

        val dateText = root.findViewById<EditText>(R.id.reportDate)
        val dateSetListener: DatePickerDialog.OnDateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                dateText.text = Editable.Factory.getInstance().newEditable(
                    "%02d/%02d/%d".format(month + 1, dayOfMonth, year))
            }

        dateText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (dateDialog == null) {
                    dateDialog = DatePickerDialog(requireContext(), dateSetListener,
                        currentDateTime.year, currentDateTime.monthValue,
                        currentDateTime.dayOfMonth)
                    dateDialog!!.show()
                }
            } else {
                dateDialog?.dismiss()
                dateDialog = null
            }
        }

        val timeText = root.findViewById<EditText>(R.id.reportTime)
        val timeSetListener: TimePickerDialog.OnTimeSetListener =
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                timeText.text = Editable.Factory.getInstance().newEditable(
                    "%d:%02d %s".format(if (hourOfDay <= 12) hourOfDay else hourOfDay - 12,
                        minute, if (hourOfDay <= 12) "AM" else "PM"))
            }

        timeText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (timeDialog == null) {
                    timeDialog = TimePickerDialog(requireContext(), timeSetListener,
                        currentDateTime.hour, currentDateTime.minute, false)
                    timeDialog!!.show()
                }
            } else {
                timeDialog?.dismiss()
                timeDialog = null
            }
        }

        val reportButton = root.findViewById<Button>(R.id.button)
        database = Firebase.database.reference

        reportButton.setOnClickListener{
            reportFB()
        }

        return root
    }

    private fun reportFB(){
        val date = requireActivity().findViewById<EditText>(R.id.reportDate)?.text.toString()
        val time = requireActivity().findViewById<EditText>(R.id.reportTime)?.text.toString()
        val noofpeople = requireActivity().findViewById<EditText>(R.id.reportNumPeople)?.text.toString()
        val currentFirebaseUserEmail = FirebaseAuth.getInstance().currentUser?.email

        if (selectedPlace == null || date.isEmpty() || noofpeople.isEmpty() || time.isEmpty()) {
            Toast.makeText(activity, "One of the above fields is empty !", Toast.LENGTH_SHORT).show()
            return
        }

        val placeLatLngString = selectedPlace!!.latLng.toString()

        database.child("report").child(placeLatLngString).child("useremail").setValue(currentFirebaseUserEmail)
        database.child("report").child(placeLatLngString).child("noofpeople").setValue(noofpeople)
        database.child("report").child(placeLatLngString).child("date").setValue(date)
        database.child("report").child(placeLatLngString).child("time").setValue(time)
    }


}