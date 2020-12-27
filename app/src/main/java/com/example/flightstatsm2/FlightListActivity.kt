package com.example.flightstatsm2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_flight_list.*


class FlightListActivity : AppCompatActivity() {

    private lateinit var viewModel: FlightListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flight_list)

        val isMobile = detail_container == null

        viewModel = ViewModelProvider(this).get(FlightListViewModel::class.java)
        viewModel.search(
            intent.getStringExtra("icao")!!,
            intent.getBooleanExtra("isArrival", false),
            intent.getLongExtra("begin", 0),
            intent.getLongExtra("end", 0)
        )

        viewModel.getSelectedFlightNameLiveData().observe(this, {
            //switch fragment
            val newFragment: FlightDetailMapFragment = FlightDetailMapFragment.newInstance()
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            if (isMobile) {
                transaction.add(R.id.activityContainer, newFragment)
                transaction.addToBackStack(null)

                transaction.commit()
            }
            else{
                transaction.add(R.id.detail_container, newFragment)
                transaction.addToBackStack(null)

                transaction.commit()
            }
        })

    }
}