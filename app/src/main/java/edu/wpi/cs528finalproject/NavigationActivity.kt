package edu.wpi.cs528finalproject

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.wpi.cs528finalproject.location.LocationHelper

class NavigationActivity : AppCompatActivity() {

    private val enableLocationManagerRequestCode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navView.setupWithNavController(navController)
        this.enableForegroundLocationFeatures(enableLocationManagerRequestCode)
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
        if (requestCode == enableLocationManagerRequestCode) {
            LocationHelper.instance().setupFusedLocationClient(this, requestCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == enableLocationManagerRequestCode) {
            if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                LocationHelper.instance().requestLocationPermissions(this, requestCode)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}