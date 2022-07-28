package com.inmobicasas.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.inmobicasas.Inmueble
import com.inmobicasas.R
import com.inmobicasas.databinding.ActivityRecyclerViewBinding
import com.inmobicasas.view.adapter.InmuebleAdapter

class RecyclerViewActivity : AppCompatActivity() {

    private lateinit var binding :ActivityRecyclerViewBinding
    var arrayList: ArrayList<Inmueble> = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRecyclerViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

       //setSupportActionBar(findViewById(R.id.toolbarrv))
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        arrayList = intent?.extras?.getSerializable("Ubicaciones") as ArrayList<Inmueble>
        Log.d("${arrayList}","msg")
        var adapter= InmuebleAdapter(arrayList)

        val manager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        //val decoration = DividerItemDecoration(this, manager.orientation)
        binding.recycler.layoutManager = manager
        binding.recycler.adapter=adapter
        //binding.recycler.addItemDecoration(decoration)
    }
}