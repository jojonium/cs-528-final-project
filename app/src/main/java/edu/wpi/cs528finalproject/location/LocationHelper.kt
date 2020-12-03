package edu.wpi.cs528finalproject.location

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.model.LatLng
import edu.wpi.cs528finalproject.PermissionUtils
import edu.wpi.cs528finalproject.R


/**
 * Uses Google Play API for obtaining device locations
 * Created by alejandro.tkachuk
 * alejandro@calculistik.com
 * www.calculistik.com Mobile Development
 */
class LocationHelper private constructor() {
    lateinit var mFusedLocationClient: FusedLocationProviderClient
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
        private val instance = LocationHelper()
        private val TAG = LocationHelper::class.java.simpleName
        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 1000
        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS: Long = 1000
        fun instance(): LocationHelper {
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
                val currentLocation: Location = locationResult.lastLocation
                val gpsPoint = LatLng(currentLocation.latitude, currentLocation.longitude)
                Log.i(TAG, "Location Callback results: $gpsPoint")
                callback(gpsPoint)
            }
        }
    }

    fun setupFusedLocationClient(activity: AppCompatActivity, requestCode: Int) {
        if (!this::mFusedLocationClient.isInitialized) {
            mFusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(activity)
            requestLocationPermissions(activity, requestCode)
        }
    }

    fun requestLocationPermissions(activity: AppCompatActivity, requestCode: Int) {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= 23) {
                PermissionUtils.requestPermission(
                    activity, requestCode,
                    Manifest.permission.ACCESS_FINE_LOCATION, false,
                    R.string.location_permission_required,
                    R.string.location_permission_rationale
                )
            }
        } else {
            mFusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback, Looper.myLooper()
            )
        }
    }
}
