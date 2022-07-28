package com.inmobicasas.view.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.inmobicasas.Configuration
import com.inmobicasas.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }


    override fun onBackPressed() {

    }

    override fun onResume() {
        super.onResume()
        if (checkPlayServices()) {
            if (checkPermissions()) {
                Thread {
                    try {
                        Thread.sleep(1500)
                    } catch (ex: Exception) {
                        Log.e(Configuration.tag, ex.message, ex)
                    }
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }.start()
            }
        }
    }


    private fun checkPlayServices(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(this@SplashActivity)
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(this, status, 2404)!!.show()
            }
            return false
        }
        return true
    }

    private fun checkPermissions(): Boolean {
        if (Build.VERSION.SDK_INT < 23) {
            return true
        } else {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ), 101
                )
            } else {
                return true
            }
        }
        return false
    }
}