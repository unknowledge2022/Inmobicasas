package com.inmobicasas

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.security.cert.CertificateException

class MapActivity : AppCompatActivity() {

    private lateinit var configuration: Configuration

    private var serviceIntent: Intent? = null
    private var myLocation: Location? = null

    var arrayList: ArrayList<Inmueble> = arrayListOf()

    lateinit var myLatLong: LatLng

    lateinit var mapFragment: SupportMapFragment

    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var usersReference: DatabaseReference

    private var retrofit: Retrofit? = null

    lateinit var userName: TextView


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        myLatLong = LatLng(.0, 0.0)

        configuration = Configuration.create(this@MapActivity)

        mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment

        Log.i("onCreate...","${configuration.defaultZoom}")
        /**
         * Firebase
         */
        firebaseDatabase = Firebase.database
        usersReference = firebaseDatabase.getReference("inmuebles")
        // My top posts by number of stars
        usersReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (i in dataSnapshot.children){
                    arrayList.add(i.getValue<Inmueble>() as Inmueble)
                    val data = i.getValue<Inmueble>() as Inmueble

                    mapFragment.getMapAsync {
                        it.addMarker(
                            MarkerOptions()
                                .position(LatLng(data.latitud!!,data.longitud!!))
                                .title(data.shortdesc)
                        )
                        var lastLatLng = LatLng(data.latitud!!,data.longitud!!)
                        Log.i("AddMarker 1","${configuration.defaultZoom} ${data.shortdesc}")
                        it.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng, configuration.defaultZoom))
                    }
                }
                Log.d("Arraylist on Data:","$arrayList")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("Inmobicasas", "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        })

        mapFragment.getMapAsync {
            it.addMarker(
                MarkerOptions()
                    .position(LatLng(0.0,0.0))
                    .title(getString(R.string.my_location))
            )
            it.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLong, configuration.defaultZoom))
            Log.i("AddMarker 2","Segundo myloc")
        }



        /**
         * Getting Markers
         */

        usersReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val value = snapshot.getValue<Inmueble>()
                Log.d(Configuration.tag, "Inmueble : $value")
                mapFragment.getMapAsync {
                    it.addMarker(
                        MarkerOptions()
                            .position(LatLng(value?.latitud!!, value?.longitud!!))
                            .title(value.shortdesc)
                    )
                    Log.i("AddMarker 3","${value.shortdesc}")
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        userName = findViewById(R.id.tvtitle)

        /**
         * Retrofit
         */
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val okHttpClient = unsafeOkHttpClient()?.addInterceptor(loggingInterceptor)?.build()

        retrofit = Retrofit.Builder()
            .baseUrl(configuration.urlBase)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()


        Log.d("Arraylist after before:","$arrayList")

        for (i in arrayList.indices){
            Log.d("Arraylist:","${arrayList.get(i)}")

        }
        Log.d("Inmobicasas..","after...")

    }

    private fun unsafeOkHttpClient(): OkHttpClient.Builder? {
        return try {
            val trustAllCerts: Array<TrustManager> = arrayOf(
                object : X509TrustManager {
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(chain: Array<X509Certificate?>?, authType: String?) {
                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(chain: Array<X509Certificate?>?, authType: String?) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                }
            )

            val sslContext: SSLContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            val sslSocketFactory: SSLSocketFactory = sslContext.getSocketFactory()
            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { _, _ -> true }
            builder
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    override fun onResume() {
        super.onResume()
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
                //center map
                //mapFragment.getMapAsync {
                 //   it.clear()
                  //  it.addMarker(
                 //       MarkerOptions()
                   //         .position(myLatLng)
                   //         .title(getString(R.string.my_location))
                  //  )
                  //  it.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, configuration.defaultZoom))
                    Log.i("AddMarker 4","ultimo ")
                //}
            }

            myLocation = location

            /**
             * Retrofit

            retrofit?.create(ServiceHub::class.java)?.postLocation(
            Inmueble(
            username = userName.text.toString(),
            latitude = extras?.get("latitude").toString().toDouble(),
            longitude = extras?.get("longitude").toString().toDouble(),
            accuracy = extras?.get("accuracy").toString().toDouble()
            )
            )?.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
            Log.d(Configuration.tag, "Response ${response.message()}")
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
            Log.e(Configuration.tag, t.message, t)
            }
            })


             */


        }
    }
}