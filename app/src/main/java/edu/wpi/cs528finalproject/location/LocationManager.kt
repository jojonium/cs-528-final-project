package edu.wpi.cs528finalproject.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.model.LatLng


/**
 * Uses Google Play API for obtaining device locations
 * Created by alejandro.tkachuk
 * alejandro@calculistik.com
 * www.calculistik.com Mobile Development
 */
class LocationManager private constructor() {
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val locationCallback: LocationCallback
    private val locationRequest: LocationRequest = LocationRequest()
    private val locationSettingsRequest: LocationSettingsRequest
    private var callback: (point: LatLng) -> Unit = {}

    fun onChange(workable: (point: LatLng) -> Unit) {
        this.callback = workable
    }

    fun getLocationSettingsRequest(): LocationSettingsRequest {
        return locationSettingsRequest
    }

    fun stop() {
        Log.i(TAG, "stop() Stopping location tracking")
        mFusedLocationClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        private val instance = LocationManager()
        private val TAG = LocationManager::class.java.simpleName
        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 1000
        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS: Long = 1000
        fun instance(): LocationManager {
            return instance
        }
    }

    init {
        locationRequest.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        locationSettingsRequest = builder.build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult) // why? this. is. retarded. Android.
                val currentLocation: Location = locationResult.getLastLocation()
                val gpsPoint = LatLng(currentLocation.latitude, currentLocation.longitude)
                Log.i(TAG, "Location Callback results: $gpsPoint")
                callback(gpsPoint)
            }
        }
    }

    fun setupFusedLocationClient(context: Context) {
        mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mFusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback, Looper.myLooper()
        )
    }
}
