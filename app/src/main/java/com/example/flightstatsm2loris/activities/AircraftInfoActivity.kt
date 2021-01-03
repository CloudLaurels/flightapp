package com.example.flightstatsm2loris.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.example.flightstatsm2loris.R
import com.example.flightstatsm2loris.fragments.FlightDetailMapFragment
import com.example.flightstatsm2loris.utils.Utils
import com.example.flightstatsm2loris.viewmodels.AircraftDetailViewModel
import com.example.flightstatsm2loris.viewmodels.FlightListViewModel
import kotlinx.android.synthetic.main.activity_flight_list.*

class AircraftInfoActivity: AppCompatActivity() {

    private lateinit var viewModel: AircraftDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aircraft_info)

        val isMobile = detail_container == null

        Utils.isMobile.value = isMobile

        viewModel = ViewModelProvider(this).get(AircraftDetailViewModel::class.java)

        viewModel.updateSelectedFlightDataAndSearch(
            intent.getStringExtra("selectedIcao")!!,
            intent.getStringExtra("selectedCallsign")!!,
            intent.getLongExtra("lastTimeSeen", 0),
            intent.getStringExtra("estDepartureAirport"),
            intent.getStringExtra("estArrivalAirport")
        )
        //viewModel.search(
//            intent.getStringExtra("icao")!!,
//            intent.getBooleanExtra("isArrival", false),
//            intent.getLongExtra("begin", 0),
//            intent.getLongExtra("end", 0)
       // )

//        viewModel.getSelectedFlightNameLiveData().observe(this, {
//            //switch fragment
//            val newFragment: FlightDetailMapFragment = FlightDetailMapFragment.newInstance()
//            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
//            if (isMobile) {
//                transaction.add(R.id.activityContainer, newFragment)
//                transaction.addToBackStack(null)
//
//                transaction.commit()
//            }
//            else{
//                transaction.add(R.id.detail_container, newFragment)
//                transaction.addToBackStack(null)
//
//                transaction.commit()
//            }
//        })

    }
}