package com.example.flightstatsm2loris.fragments


import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.flightstatsm2loris.R
import com.example.flightstatsm2loris.activities.AircraftInfoActivity
import com.example.flightstatsm2loris.utils.Utils
import com.example.flightstatsm2loris.viewmodels.FlightListViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.fragment_flight_detail_map.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FlightDetailMapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FlightDetailMapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var viewModel: FlightListViewModel
    private lateinit var mMapView: MapView
    private lateinit var myGoogleMap: GoogleMap

    private var depCoordinates: LatLng? = null
    private var arrCoordinates: LatLng? = null

    private var mapLoaded = false


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
       val rootView: View = inflater.inflate(R.layout.fragment_flight_detail_map, container, false)
        mMapView = rootView.findViewById(R.id.mapView) as MapView
        mMapView.onCreate(savedInstanceState)
        mMapView.onResume()

        mMapView.getMapAsync(this)

        // Inflate the layout for this fragment
        return rootView
        //return inflater.inflate(R.layout.fragment_flight_detail_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button.setOnClickListener { moreInfoOnPlane() }

        viewModel = ViewModelProvider(requireActivity()).get(FlightListViewModel::class.java)

    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mMapView.onSaveInstanceState(outState)
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            FlightDetailMapFragment().apply {

            }
    }

    fun updateMapView() {

        myGoogleMap.clear()

        val depIcon = Utils.generateSmallIcon(context!!, R.drawable.airplane)
        val arrIcon = Utils.generateSmallIcon(context!!, R.drawable.airplane)

        val poi = ArrayList<LatLng>()
        val polyLineOptions = PolylineOptions()

        if (depCoordinates != null) {
            myGoogleMap.addMarker(
                MarkerOptions()
                    .position(depCoordinates!!)
                    .title("Departure airport")
                    .icon(BitmapDescriptorFactory.fromBitmap(depIcon))
                    .anchor(0.5f, 0.5f)
            )
            poi.add(depCoordinates!!) //from
        }

        if (arrCoordinates != null) {
            myGoogleMap.addMarker(
                MarkerOptions()
                    .position(arrCoordinates!!)
                    .title("Arrival airport")
                    .icon(BitmapDescriptorFactory.fromBitmap(arrIcon))
                    .anchor(0.5f, 0.5f)
            )
            poi.add(arrCoordinates!!)
        }

        polyLineOptions.width(7f)
        polyLineOptions.geodesic(true)
        polyLineOptions.color(Color.BLUE)
        polyLineOptions.addAll(poi)
        val polyline: Polyline = myGoogleMap.addPolyline(polyLineOptions)
        polyline.isGeodesic = true

        if (mapLoaded) {
            onMapLoaded()
        }
    }

    private fun moreInfoOnPlane() {
        val i = Intent(activity, AircraftInfoActivity::class.java)
        i.putExtra("selectedIcao", viewModel.getSelectedFlightLiveData().value!!.icao24)
        i.putExtra("lastTimeSeen", viewModel.getSelectedFlightLiveData().value!!.lastSeen)
        i.putExtra(
            "estDepartureAirport",
            viewModel.getSelectedFlightLiveData().value!!.estDepartureAirport
        )
        i.putExtra(
            "estArrivalAirport",
            viewModel.getSelectedFlightLiveData().value!!.estArrivalAirport
        )
        startActivity(i)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        myGoogleMap = googleMap

        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(context, R.raw.style_json)
            )
            if (!success) {
                Log.e("map styling", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("map styling", "Can't find style. Error: ", e)
        }

        viewModel.getAirportsDetailLiveData().observe(this, {
            if (it == null) {
                depCoordinates = null
                arrCoordinates = null
            } else {
                depCoordinates = it["departure"]
                arrCoordinates = it["arrival"]
            }

            updateMapView()
        })
        myGoogleMap.setOnMapLoadedCallback(this)
    }


    override fun onMapLoaded() {
        if (depCoordinates != null && arrCoordinates != null) {
            this.zoomToFit(depCoordinates!!, arrCoordinates!!)
        }
        mapLoaded = true
    }


    private fun zoomToFit(poi1: LatLng, poi2: LatLng) {
        val group = LatLngBounds.Builder()
            .include(poi1) // LatLgn object1
            .include(poi2) // LatLgn object2
            .build()

        myGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(group, 400))
    }
}