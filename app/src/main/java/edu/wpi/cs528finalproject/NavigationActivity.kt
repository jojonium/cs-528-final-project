package edu.wpi.cs528finalproject

import android.Manifest
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.wpi.cs528finalproject.location.LocationHelper
import edu.wpi.cs528finalproject.ui.home.HomeFragment

class NavigationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navView.setupWithNavController(navController)
        this.enableForegroundLocationFeatures(PermissionRequestCodes.enableLocationHelper)
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
        if (requestCode == PermissionRequestCodes.enableLocationHelper) {
            LocationHelper.instance().setupFusedLocationClient(this, requestCode)
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
            if (requestCode == PermissionRequestCodes.enableLocationHelper) {
                if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    LocationHelper.instance().requestLocationPermissions(this, requestCode)
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
                if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                    val homeFragment = navHostFragment.childFragmentManager.primaryNavigationFragment as HomeFragment
                    homeFragment.requestLocationPermissions()
                    if (DeferredPermissions.deferredMap[PermissionRequestCodes.enableLocationHelper] == true) {
                        LocationHelper.instance().requestLocationPermissions(this, requestCode)
                        DeferredPermissions.deferredMap[PermissionRequestCodes.enableLocationHelper] = false
                    }
                } else {
                    DeferredPermissions.deferredMap[PermissionRequestCodes.enableLocationHelper] = false
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}