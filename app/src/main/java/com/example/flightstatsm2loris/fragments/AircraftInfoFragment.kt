package com.example.flightstatsm2loris.fragments


import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.flightstatsm2loris.viewmodels.FlightListViewModel
import com.example.flightstatsm2loris.R
import com.example.flightstatsm2loris.utils.Utils
import com.example.flightstatsm2loris.viewmodels.AircraftDetailViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.fragment_airplane_info.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class AircraftInfoFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var viewModel: AircraftDetailViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       val rootView: View = inflater.inflate(R.layout.fragment_airplane_info, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(AircraftDetailViewModel::class.java)

        viewModel.getCurrentAircraftData().observe(this, {
            callSign.text = it.callsign
            lastSeenTime.text = it.lastContact.toString()
            icao.text = it.icao
            country.text = it.originCountry
            state.text = it.getLitteralState()
            speed.text = it.velocity.toString()
            altgeo.text = it.geoAltitude.toString()
            altbaro.text = it.baroAltitude.toString()
            verticalRate.text = it.verticalRate.toString()
            source.text = it.positionSource.toString()
        })

        // Inflate the layout for this fragment
        return rootView
        //return inflater.inflate(R.layout.fragment_flight_detail_map, container, false)
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            AircraftInfoFragment().apply {

            }
    }
}