package com.example.flightstatsm2loris.fragments


import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.flightstatsm2loris.R
import com.example.flightstatsm2loris.utils.Utils
import com.example.flightstatsm2loris.viewmodels.AircraftDetailViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import java.util.*
import kotlin.collections.ArrayList


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class AircraftDetailMapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    val PATTERN_DASH_LENGTH_PX = 20
    val PATTERN_GAP_LENGTH_PX = 20
    val DOT: PatternItem = Dot()
    val DASH: PatternItem = Dash(PATTERN_DASH_LENGTH_PX.toFloat())
    val GAP: PatternItem = Gap(PATTERN_GAP_LENGTH_PX.toFloat())
    val PATTERN_POLYGON_ALPHA: List<PatternItem> = Arrays.asList(GAP, DASH)

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
       val rootView: View = inflater.inflate(
           R.layout.fragment_airplane_map_detail,
           container,
           false
       )

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
            val airplaneIcon = Utils.generateMediumIcon(context!!, R.drawable.airplane)
            Log.i("Rotation check", it.currentOrientation.toString())
            if (it.latitude != null && it.longitude != null) {

                val airplaneLocation = LatLng(it.latitude, it.longitude!!)

                myGoogleMap.addMarker(
                    MarkerOptions()
                        .position(airplaneLocation)
                        .icon(BitmapDescriptorFactory.fromBitmap(airplaneIcon))
                        .anchor(0.5f, 0.5f)
                        .rotation(it.currentOrientation!!.toFloat())
                        .zIndex(999F)
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
                    poi.add(it1["departure"]!!) //from
                    poi.add(airplaneLocation) // to
                    polyLineOptions.width(7f)
                    polyLineOptions.geodesic(true)
                    polyLineOptions.color(Color.YELLOW)
                    polyLineOptions.addAll(poi)
                    val polyline: Polyline = myGoogleMap.addPolyline(polyLineOptions)
                    polyline.isGeodesic = true


                    val pois = ArrayList<LatLng>()
                    pois.add(airplaneLocation) //from
                    pois.add(it1["arrival"]!!) // to
                    val polyOptions = PolylineOptions()
                    polyOptions.color(Color.YELLOW)
                    polyOptions.addAll(pois)
                    polyOptions.pattern(PATTERN_POLYGON_ALPHA)
                    val polyline1 = googleMap.addPolyline(polyOptions)
                    polyline1.isGeodesic = true


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
        var padding = 6F

        // C'est pas correct, il faut ajouter un padding Y pour que le marker
        // n'apparaisse pas en dessous du fragment d'infos
        if (Utils.isMobile.value == true) {
            padding = 3F
            Log.i("Mobile zoom", "We are on mobile")
        }

        myGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, padding))
        zoomed = true
    }

    private fun zoomToFit(poi1: LatLng, poi2: LatLng) {
        val group = LatLngBounds.Builder()
            .include(poi1) // LatLgn object1
            .include(poi2) // LatLgn object2
            .build()

        var padding = 400

        // C'est pas correct, il faut ajouter un padding Y pour que le marker
        // n'apparaisse pas en dessous du fragment d'infos
        if (Utils.isMobile.value == true) {
            padding = 700
            Log.i("Mobile zoom", "We are on mobile")
        }

        myGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(group, padding))
        zoomed = true
    }


}