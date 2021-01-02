package com.example.flightstatsm2loris.fragments


import android.content.res.Resources
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

    private var zoomed = false



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

        viewModel.getCurrentAircraftData().observe(this, {
            val airplaneIcon = Utils.generateSmallIcon(context!!, R.drawable.airplane)
            Log.i("Rotation check", it.currentOrientation.toString())
            if (it.latitude != null && it.longitude != null) {

                val airplaneLocation = LatLng(it.latitude, it.longitude!!)

                myGoogleMap.addMarker(
                    MarkerOptions()
                        .position(airplaneLocation)
                        .icon(BitmapDescriptorFactory.fromBitmap(airplaneIcon))
                        .anchor(0.5f, 0.5f)
                        .rotation(it.currentOrientation!!.toFloat())
                )

                viewModel.selectedFlightRouteCoordinates.observe(this, { it1 ->
                    val depIcon = Utils.generateSmallIcon(context!!, R.drawable.departuretower)
                    val arrIcon = Utils.generateSmallIcon(context!!, R.drawable.arrivaltower)

                    myGoogleMap.addMarker(
                        MarkerOptions()
                            .position(it1["departure"]!!)
                            .title("Departure airport")
                            .icon(BitmapDescriptorFactory.fromBitmap(depIcon))
                            .anchor(0.5f, 0.5f)
                    )

                    myGoogleMap.addMarker(
                        MarkerOptions()
                            .position(it1["arrival"]!!)
                            .title("Arrival airport")
                            .icon(BitmapDescriptorFactory.fromBitmap(arrIcon))
                            .anchor(0.5f, 0.5f)
                    )

                    val poi = ArrayList<LatLng>()
                    val polyLineOptions = PolylineOptions()
                    poi.add(airplaneLocation) //from
                    poi.add(it1["arrival"]!!) // to
                    polyLineOptions.width(7f)
                    polyLineOptions.geodesic(true)
                    polyLineOptions.color(Color.YELLOW)
                    polyLineOptions.addAll(poi)
                    val polyline: Polyline = myGoogleMap.addPolyline(polyLineOptions)
                    polyline.isGeodesic = true

                    if (!zoomed) {
                        zoomToFit(airplaneLocation, it1["arrival"]!!)
                    }
                })

                if (!zoomed) {
                    zoomToAirplane(airplaneLocation)
                }
            }
        })




    }




    override fun onMapLoaded() {
        //this.zoomToFit(depCoordinates, arrCoordinates)
    }

    private fun zoomToAirplane(pos: LatLng) {
        myGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 6F))
        zoomed = true
    }

    private fun zoomToFit(poi1: LatLng, poi2: LatLng) {
        val group = LatLngBounds.Builder()
            .include(poi1) // LatLgn object1
            .include(poi2) // LatLgn object2
            .build()

        myGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(group, 400))
        zoomed = true
    }
}