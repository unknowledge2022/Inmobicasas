package com.inmobicasas.view.activity

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import com.inmobicasas.*
import com.inmobicasas.R
import retrofit2.Retrofit

class MapActivity : AppCompatActivity() {

    private lateinit var configuration: Configuration

    private var serviceIntent: Intent? = null
    private var myLocation: Location? =null
    var arrayList: ArrayList<Inmueble> = arrayListOf()


    lateinit var myLatLong: LatLng

    lateinit var mapFragment: SupportMapFragment




    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        configuration = Configuration.create(this@MapActivity)

        mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        myLatLong = LatLng(.0, 0.0)

        arrayList = intent?.extras?.getSerializable("Ubicaciones") as ArrayList<Inmueble>

        mostrarMarkers()
    }

    fun mostrarMarkers(){
        if(arrayList.count()!=0) {
            mapFragment.getMapAsync() {
                it.clear()
            }
            for (i in arrayList.indices) {
               // Log.d("${arrayList.get(i).departamento}", "Msg")
                mapFragment.getMapAsync {
                    it.addMarker(
                        MarkerOptions()
                            .position(
                                LatLng(
                                    arrayList.get(i).latitud!!,
                                    arrayList.get(i).longitud!!
                                )
                            )
                            .title(arrayList.get(i).shortdesc)
                            .snippet(arrayList.get(i).precio)
                            //.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_houseclaro_foreground)).anchor(0.0f,1.0f)
                    )
                    var lastLatLng =
                        LatLng(arrayList.get(i).latitud!!, arrayList.get(i).longitud!!)

                    it.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            lastLatLng,
                            configuration.defaultZoom
                        )
                    )
                }
            }
        }
        else{
           // Log.d("Error ${arrayList.count()}","Msg")
        }
    }


    override fun onResume() {
        super.onResume()

        mostrarMarkers()

        if (isAppOnForeground(applicationContext)) {
            LocalBroadcastManager.getInstance(applicationContext)
                .registerReceiver(messageReceiver, IntentFilter(Configuration.tag))
            if (serviceIntent == null) {
                serviceIntent = Intent(applicationContext, Foreground::class.java)
            }
            ContextCompat.startForegroundService(applicationContext, serviceIntent!!)
        }
    }

    override fun onPause() {
        super.onPause()

        mostrarMarkers()

        LocalBroadcastManager.getInstance(applicationContext)
            .unregisterReceiver(messageReceiver)
        if (serviceIntent != null) {
            stopService(serviceIntent)
        }
    }

    private fun isAppOnForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = context.packageName
        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == packageName) {
                return true
            }
        }
        return false
    }

    private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val extras = intent.extras
            val location = Location(
                latitude = extras?.get("latitude").toString().toDouble(),
                longitude = extras?.get("longitude").toString().toDouble(),
                accuracy = extras?.get("accuracy").toString().toDouble()
            )

            Log.d(Configuration.tag, "Location [${location.latitude};${location.longitude} / ${location.accuracy}]}")

            if (myLocation == null) {
                var myLatLng = LatLng(location.latitude, location.longitude)

            }

            myLocation = location

        }
    }
}