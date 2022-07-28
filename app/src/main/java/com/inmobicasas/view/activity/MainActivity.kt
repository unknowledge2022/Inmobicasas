package com.inmobicasas.view.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.inmobicasas.Configuration
import com.inmobicasas.Inmueble
import com.inmobicasas.R
import com.inmobicasas.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    //private var myLocation: In? = null
    lateinit var binding: ActivityMainBinding
    lateinit var itemDepto: String
    lateinit var itemCiudad: String
    lateinit var itemCriterio: String
    private lateinit var configuration: Configuration
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var usersReference: DatabaseReference
    var arrayList: ArrayList<Inmueble> = arrayListOf()
    var arrayListResult: ArrayList<Inmueble> = arrayListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        val adaptordepto = ArrayAdapter.createFromResource(this, R.array.departamentos,android.R.layout.simple_spinner_item)

        binding.spindepto.adapter = adaptordepto
        binding.spindepto.setSelection(0)


        val adaptertipo = ArrayAdapter.createFromResource(this, R.array.tipo, android.R.layout.simple_spinner_item)
        binding.spintipo.adapter = adaptertipo
        binding.spintipo.setSelection(0)

        val adaptercriterio = ArrayAdapter.createFromResource(this, R.array.criterio, android.R.layout.simple_spinner_item)
        binding.spincriterio.adapter = adaptercriterio


        binding.spindepto.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                itemDepto = p0?.getItemAtPosition(p2).toString()
                when (p2){
                    0->{
                        val adaptorciudad = ArrayAdapter.createFromResource(this@MainActivity,
                        R.array.ciudadessantacruz,android.R.layout.simple_spinner_item)
                        binding.spinciudad.adapter = adaptorciudad
                        binding.spinciudad.setSelection(0)
                    }
                    3->{
                        val adaptorciudad = ArrayAdapter.createFromResource(this@MainActivity,
                        R.array.ciudadescbba,android.R.layout.simple_spinner_item)
                        binding.spinciudad.adapter = adaptorciudad
                        binding.spinciudad.setSelection(0)
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        binding.spinciudad.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                itemCiudad = p0?.getItemAtPosition(p2).toString()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }
        binding.spincriterio.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                itemCriterio = p0?.getItemAtPosition(p2).toString()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                Toast.makeText(this@MainActivity, "Debe Seleccionar un criterio",Toast.LENGTH_SHORT).show()
            }

        }
        obtenerDatos()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    fun onClickMostrarRV(view: View) {
        var intent: Intent? = null
        when(itemCriterio){
            "Departamento"->{
                buscar(itemDepto)
            }
            "Ciudad"->{
                buscar(itemCiudad)
            }
        }
        intent = Intent(this@MainActivity, RecyclerViewActivity::class.java)
        intent?.putExtra("Ubicaciones", arrayListResult)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        var intent: Intent? = null
        when(itemCriterio){
            "Departamento"->{
                buscar(itemDepto)
            }
            "Ciudad"->{
                buscar(itemCiudad)
            }
        }
        when (item.itemId) {
            R.id.action_map -> {
                intent = Intent(this@MainActivity, MapActivity::class.java)
            }
            R.id.action_config -> {
                Log.d(Configuration.tag, "Config")
            }
        }
        intent?.putExtra("Ubicaciones", arrayListResult)
        startActivity(intent)
        return super.onOptionsItemSelected(item)
    }

    fun onClickMostrarResByDepto(view: View) {
        var intent: Intent?
        when(itemCriterio){
            "Departamento"->{
                buscar(itemDepto)
            }
            "Ciudad"->{
                buscar(itemCiudad)
            }
        }
        intent = Intent(this@MainActivity, MapActivity::class.java)
        intent?.putExtra("Ubicaciones", arrayListResult)
        startActivity(intent)
    }

    fun buscar(valor: String){
        if(arrayList.isEmpty())
            obtenerDatos()
        arrayListResult.clear()
        if(itemCriterio=="Departamento"){
            for (i in arrayList.indices) {
                if (arrayList.get(i).departamento == itemDepto) {
                    arrayListResult.add(arrayList.get(i))
                    //Log.d("${arrayList.get(i).departamento}","Msg")
                }
            }
        }
        else{
            for (i in arrayList.indices){
                if(arrayList.get(i).ciudad==valor){
                    arrayListResult.add(arrayList.get(i))
                }
            }
        }
        //Log.d("${arrayList.get(0)}","Msg")
    }



    private fun obtenerDatos(){
        configuration = Configuration.create(this@MainActivity)

        /**
         * Firebase
         */
        firebaseDatabase = Firebase.database
        usersReference = firebaseDatabase.getReference("inmuebles")

        usersReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                arrayList.clear()

                for (i in dataSnapshot.children){
                    arrayList.add(i.getValue<Inmueble>() as Inmueble)
                    //val data = i.getValue<Inmueble>() as Inmueble
                }
               // Log.d("Arraylist on DataChange:","$arrayList")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("Inmobicasas", "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        })

    }

    fun onClickMostrarResByCity(view: View) {
       // buscarPorCiudad()
    }

}