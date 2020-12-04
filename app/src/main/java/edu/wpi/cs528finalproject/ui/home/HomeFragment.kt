package edu.wpi.cs528finalproject.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
import edu.wpi.cs528finalproject.location.LocationHelper

const val defaultZoom: Float = 16.0F

class HomeFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnPoiClickListener,
GoogleMap.OnMapClickListener {

    private lateinit var homeViewModel: HomeViewModel
    private var mMap: GoogleMap? = null
    private var mapView: MapView? = null
    private var marker: Marker? = null
    private var selectedPoi: PointOfInterest? = null

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
//        val textView: TextView = root.findViewById(R.id.text_home)
//        homeViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        return root
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