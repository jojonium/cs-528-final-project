package edu.wpi.cs528finalproject

import android.Manifest
import android.content.*
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.libraries.places.api.Places
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.wpi.cs528finalproject.location.LocationUpdatesService
import edu.wpi.cs528finalproject.ui.home.HomeFragment

interface LocationChangedListener {
    fun onLocationChanged(location: Location?)
}

class NavigationActivity : AppCompatActivity() {

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private var myReceiver: MyReceiver? = null

    // A reference to the service used to get location updates.
    private var mService: LocationUpdatesService? = null

    // Tracks the bound state of the service.
    private var mBound = false

    /**
     * Receiver for broadcasts sent by [LocationUpdatesService].
     */
    private class MyReceiver : BroadcastReceiver() {
        var listeners: MutableList<LocationChangedListener> = mutableListOf()
        override fun onReceive(context: Context, intent: Intent) {
            val location: Location? =
                intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION)

            for (listener in listeners) {
                listener.onLocationChanged(location)
            }
        }

        fun addOnLocationChangedListener(listener: LocationChangedListener) {
            listeners.add(listener)
        }
    }

    fun addOnLocationChangedListener(listener: LocationChangedListener) {
        myReceiver!!.addOnLocationChangedListener(listener)
    }

    // Monitors the state of the connection to the service.
    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder: LocationUpdatesService.LocalBinder = service as LocationUpdatesService.LocalBinder
            mService = binder.service
            mService?.setCurrentActivity(this@NavigationActivity)
            mBound = true
            enableForegroundLocationFeatures(PermissionRequestCodes.enableLocationUpdatesService)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mService = null
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Places.initialize(applicationContext, getString(R.string.google_places_key))
        myReceiver = MyReceiver()
        setContentView(R.layout.activity_navigation)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navView.setupWithNavController(navController)
    }

    override fun onStart() {
        super.onStart()
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(
            Intent(this, LocationUpdatesService::class.java), mServiceConnection,
            BIND_AUTO_CREATE
        )
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            myReceiver!!,
            IntentFilter(LocationUpdatesService.ACTION_BROADCAST)
        )
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver!!)
        super.onPause()
    }

    override fun onStop() {
        if (mBound) {
            unbindService(mServiceConnection)
            mBound = false
        }
        super.onStop()
    }

    override fun onBackPressed() {
        val webView = findViewById<WebView>(R.id.webview)
        if (webView != null && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private fun enableForegroundLocationFeatures(requestCode: Int) {
        if (requestCode == PermissionRequestCodes.enableLocationUpdatesService) {
            mService?.requestLocationUpdates(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (permissions.isEmpty()) {
            DeferredPermissions.deferredMap[requestCode] = true
        } else {
            if (requestCode == PermissionRequestCodes.enableLocationUpdatesService) {
                if (PermissionUtils.isPermissionGranted(
                        permissions, grantResults,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )) {
                    mService?.requestLocationUpdates(this)
                    if (DeferredPermissions.deferredMap[PermissionRequestCodes.enableMapView] == true) {
                        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                        val homeFragment = navHostFragment.childFragmentManager.primaryNavigationFragment as HomeFragment
                        homeFragment.requestLocationPermissions()
                        DeferredPermissions.deferredMap[PermissionRequestCodes.enableMapView] = false
                    }
                } else {
                    DeferredPermissions.deferredMap[PermissionRequestCodes.enableMapView] = false
                }
            } else if (requestCode == PermissionRequestCodes.enableMapView) {
                if (PermissionUtils.isPermissionGranted(
                        permissions, grantResults,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )) {
                    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                    val homeFragment = navHostFragment.childFragmentManager.primaryNavigationFragment as HomeFragment
                    homeFragment.requestLocationPermissions()
                    if (DeferredPermissions.deferredMap[PermissionRequestCodes.enableLocationUpdatesService] == true) {
                        mService?.requestLocationUpdates(this)
                        DeferredPermissions.deferredMap[PermissionRequestCodes.enableLocationUpdatesService] = false
                    }
                } else {
                    DeferredPermissions.deferredMap[PermissionRequestCodes.enableLocationUpdatesService] = false
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}