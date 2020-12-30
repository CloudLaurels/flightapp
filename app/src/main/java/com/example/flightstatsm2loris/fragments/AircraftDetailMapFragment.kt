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
import kotlinx.android.synthetic.main.fragment_airplane_map_detail.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class AircraftDetailMapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var viewModel: AircraftDetailViewModel
    private lateinit var mMapView: MapView
    private lateinit var myGoogleMap: GoogleMap



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
       val rootView: View = inflater.inflate(R.layout.fragment_airplane_map_detail, container, false)

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

        mMapView = rootView.findViewById(R.id.aircraftMapView) as MapView
        mMapView.onCreate(savedInstanceState)
        mMapView.onResume()

        mMapView.getMapAsync(this)

        // Inflate the layout for this fragment
        return rootView
        //return inflater.inflate(R.layout.fragment_flight_detail_map, container, false)
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            AircraftDetailMapFragment().apply {

            }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        myGoogleMap = googleMap
        myGoogleMap.setOnMapLoadedCallback(this)


//        Log.e("Mapfragment", "Dep airport" + viewModel.getDepartureAirportCoordinates())
//        Log.e("Mapfragment", "Arrival airport" + viewModel.getArrivalAirportCoordinates())
//
//        val depIcon = Utils.generateSmallIcon(context!!, R.drawable.airplane)
//        val arrIcon = Utils.generateSmallIcon(context!!, R.drawable.airplane)
//
//        myGoogleMap.addMarker(
//            MarkerOptions()
//                .position(depCoordinates)
//                .title("Departure airport")
//                .icon(BitmapDescriptorFactory.fromBitmap(depIcon))
//                .anchor(0.5f, 0.5f)
//        )
//
//        myGoogleMap.addMarker(
//            MarkerOptions()
//                .position(arrCoordinates)
//                .title("Arrival airport")
//                .icon(BitmapDescriptorFactory.fromBitmap(arrIcon))
//                .anchor(0.5f, 0.5f)
//        )
//
//
//        val poi = ArrayList<LatLng>()
//        val polyLineOptions = PolylineOptions()
//        poi.add(depCoordinates) //from
//        poi.add(arrCoordinates) // to
//        polyLineOptions.width(7f)
//        polyLineOptions.geodesic(true)
//        polyLineOptions.color(Color.BLUE)
//        polyLineOptions.addAll(poi)
//        val polyline: Polyline = myGoogleMap.addPolyline(polyLineOptions)
//        polyline.isGeodesic = true

    }




    override fun onMapLoaded() {
        //this.zoomToFit(depCoordinates, arrCoordinates)
    }

    private fun zoomToFit(poi1: LatLng, poi2: LatLng) {
        val group = LatLngBounds.Builder()
            .include(poi1) // LatLgn object1
            .include(poi2) // LatLgn object2
            .build()

        myGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(group, 400))
    }
}