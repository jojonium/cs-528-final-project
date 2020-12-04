package edu.wpi.cs528finalproject.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.beust.klaxon.Klaxon
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
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
import edu.wpi.cs528finalproject.PermissionRequestCodes
import edu.wpi.cs528finalproject.PermissionUtils
import edu.wpi.cs528finalproject.R
import edu.wpi.cs528finalproject.location.CityData
import edu.wpi.cs528finalproject.location.LocationHelper
import java.lang.Error
import java.util.*

const val defaultZoom: Float = 16.0F

class HomeFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnPoiClickListener,
GoogleMap.OnMapClickListener {

    private lateinit var homeViewModel: HomeViewModel
    private var mMap: GoogleMap? = null
    private var mapView: MapView? = null
    private var marker: Marker? = null
    private var selectedPoi: PointOfInterest? = null
    private var previousCity = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_home, container, false)
        mapView = root.findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
        LocationHelper.instance().onChange {
            val geocoder = Geocoder(this.requireContext(), Locale.getDefault())
            val address = geocoder.getFromLocation(it.latitude, it.longitude, 1)[0]
            val city = address.locality
            if (city != previousCity) {
                previousCity = city
                Fuel.post("http://covidtraveler-env.eba-2ze4syip.us-east-2.elasticbeanstalk.com/")
                        .jsonBody("{ \"town\": \"$city\" }")
                        .response { request, response, result ->
                            this.handleCityDataResponse(request, response, result)
                        }
            }
        }
        return root
    }

    private fun handleCityDataResponse(request: Request, response: Response, result: Result<ByteArray, FuelError>) {
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
        var cityData: CityData? = null
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
        // TODO implement
    }

    private fun displayDataError(message: String) {
        Log.e("CityAPI", "ERR: $message")
        // TODO implement
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
            val loc = LocationHelper.instance(mMap).currentLocation
            if (loc != null) {
                mMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            loc.latitude,
                            loc.longitude
                        ), defaultZoom
                    )
                )
            }
        }
    }

    // TODO: Add geocoding to background service to detect when user enters a new town
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