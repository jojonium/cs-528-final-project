package edu.wpi.cs528finalproject.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.beust.klaxon.Klaxon
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.result.Result
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import edu.wpi.cs528finalproject.*
import edu.wpi.cs528finalproject.location.CityData
import java.util.*

class HomeFragment :
        Fragment(),
        OnMapReadyCallback,
        GoogleMap.OnPoiClickListener,
        GoogleMap.OnMapClickListener {

    private lateinit var homeViewModel: HomeViewModel
    private var mMap: GoogleMap? = null
    private var mapView: MapView? = null
    private val defaultZoom: Float = 16.0F
    private lateinit var alertTextNumCasesView: TextView
    private var marker: Marker? = null
    private var selectedPoi: PointOfInterest? = null
    private var previousCity = ""
    private var currentLocation: Location? = null


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_home, container, false)

        alertTextNumCasesView = root.findViewById(R.id.alertTextNumCases)

        mapView = root.findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
        (requireActivity() as NavigationActivity).addOnLocationChangedListener(object: LocationChangedListener {
            override fun onLocationChanged(location: Location?) {
                if (location != null) {
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)[0]
                    val city = address.locality
                    if (city != previousCity) {
                        previousCity = city
                        Fuel.post("http://covidtraveler-env.eba-2ze4syip.us-east-2.elasticbeanstalk.com/")
                            .jsonBody("{ \"town\": \"$city\" }")
                            .response { _, response, result ->
                                handleCityDataResponse(response, result)
                            }
                    }
                    if (currentLocation == null) {
                        mMap?.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    location.latitude,
                                    location.longitude
                                ), defaultZoom
                            )
                        )
                    }
                    currentLocation = location
                }
            }
        })
        return root
    }

    private fun handleCityDataResponse(response: Response, result: Result<ByteArray, FuelError>) {
        val (bytes, error) = result
        if (bytes == null || error != null) {
            displayDataError("An error occurred while fetching COVID data from the network.")
            Log.e("CityAPI", error.toString())
            return;
        }
        if (response.statusCode == 404 || bytes.isEmpty()) {
            displayDataError("No COVID data is available for your current location.")
            return;
        }
        val json = String(response.data)
        Log.d("CityAPI", json)
        val cityData: CityData?
        try {
            cityData = Klaxon()
                    .parseArray<CityData>(json)?.get(0) ?: throw Error("cityData is null")
        } catch (error: Error) {
            Log.e("CityAPI", error.toString())
            displayDataError("Received an invalid response from the server.")
            return;
        }
        displayCityData(cityData)
    }

    private fun displayCityData(cityData: CityData) {
        activity?.runOnUiThread(Runnable {
            alertTextNumCasesView.text = resources.getString(R.string.safetyTextNumCases, cityData.twoWeekCaseCounts)
        })
    }

    private fun displayDataError(message: String) {
        Log.e("CityAPI", "ERR: $message")
        activity?.runOnUiThread(Runnable {
            alertTextNumCasesView.text = message
        })
    }

    fun requestLocationPermissions() {
        if (mMap == null) return
        if (ActivityCompat.checkSelfPermission(
                        this.requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this.requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= 23) {
                PermissionUtils.requestPermission(
                        this.requireActivity() as AppCompatActivity, PermissionRequestCodes.enableMapView,
                        Manifest.permission.ACCESS_FINE_LOCATION, false,
                        R.string.location_permission_required,
                        R.string.location_permission_rationale
                )
            }
        } else {
            mMap?.isMyLocationEnabled = true
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        mMap?.setOnPoiClickListener(this)
        mMap?.setOnMapClickListener(this)
        requestLocationPermissions()
    }

    override fun onMapClick(latLng: LatLng) {
        marker?.remove()
        marker = null
        selectedPoi = null
    }

    override fun onPoiClick(poi: PointOfInterest) {
        selectedPoi = poi
        marker = mMap!!.addMarker(
                // TODO: Add COVID data to marker info window
                MarkerOptions()
                        .position(poi.latLng)
        )
        mMap?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(poi.latLng, defaultZoom)
        )
    }

    override fun onResume() {
        mapView?.onResume()
        super.onResume()
    }

    override fun onPause() {
        mapView?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView?.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        mapView?.onLowMemory()
        super.onLowMemory()
    }
}