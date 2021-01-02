package com.example.flightstatsm2loris.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flightstatsm2loris.models.Aircraft
import com.example.flightstatsm2loris.models.Airport
import com.example.flightstatsm2loris.models.FlightModel
import com.example.flightstatsm2loris.network.RequestsManager
import com.example.flightstatsm2loris.utils.Utils
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

class AircraftDetailViewModel : ViewModel(), RequestsManager.RequestListener {

    val isLoadingLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private val selectedAircraftLiveData: MutableLiveData<Aircraft> = MutableLiveData()

    val aircraftICAO: MutableLiveData<String> = MutableLiveData()
    val aircraftCallSign: MutableLiveData<String> = MutableLiveData()
    val aircraftLastSeen: MutableLiveData<Long> = MutableLiveData()
    val aircraftEstDepartureAirport: MutableLiveData<String> = MutableLiveData()
    val aircraftEstArrivalAirport: MutableLiveData<String> = MutableLiveData()

    val isAircraftOnline: MutableLiveData<Boolean> = MutableLiveData()

    val aircraftFlightListLiveData: MutableLiveData<List<FlightModel>> = MutableLiveData()

    private val airportListLiveData : MutableLiveData<List<Airport>> = MutableLiveData()
    val areRoutesLoaded: MutableLiveData<Boolean> = MutableLiveData()
    init {
        airportListLiveData.value = Utils.generateAirportList()
    }

    val selectedFlightRouteCoordinates: MutableLiveData<HashMap<String, LatLng>> = MutableLiveData()
    private val routeDepartureAirportCoords: MutableLiveData<LatLng> = MutableLiveData()
    private val routeArrivalAirportCoords: MutableLiveData<LatLng> = MutableLiveData()


    override fun onRequestSuccess(result: String?) {
        TODO("Not yet implemented")
    }

    override fun onRequestFailed() {
        TODO("Not yet implemented")
    }


    // On cherche d'abord à avoir les données en live de l'avion
    // si on en a pas on va les chercher au moment où il a été aperçu pour la dernière fois
    fun searchAircraftData(live: Boolean) {
        val baseUrl: String = "https://opensky-network.org/api/states/all"
        viewModelScope.launch {
            isLoadingLiveData.value = true
            val result = withContext(Dispatchers.IO) {
                RequestsManager.getSuspended(baseUrl, getAircraftRequestParams(live))
            }
            isLoadingLiveData.value = false
            if (result == null) {
                Log.e("Request", "Empty request response")
            } else {

                if ((JSONObject(result)["states"])::class != JSONArray::class) {
                    Log.e("Parsing aircraft data", "Aircraft is offline, states are null, we should specify a time, now trying with last time seen")
                    isAircraftOnline.value = false
                    searchAircraftData(false)
                } else {
                    // Pas très classe/safe comme façon de faire certes,
                    // je n'arrive pas à parser correctement avec des index
                    // (pas de key dans la réponse API)
                    val states = (JSONObject(result)["states"] as JSONArray)[0] as JSONArray

                    // Bien que l'api (https://opensky-network.org/apidoc/rest.html#all-state-vectors)
                    // précise que la plupart des propriétés sont des Floats, il arrive
                    // qu'elle renvoie des Integers voire null de temps à autres ??????????
                    var vertRate = states[11]
                    if (vertRate::class == Int::class) {
                        vertRate = (vertRate as Int).toDouble()
                    }

                    var baroAltitude = states[7]
                    if (baroAltitude::class == Int::class) {
                        baroAltitude = (baroAltitude as Int).toDouble()
                    } else if (baroAltitude == null) {
                        baroAltitude = 0.0
                    }

                    var geoAltitude = states[13]
                    if (geoAltitude::class == Int::class) {
                        geoAltitude = (geoAltitude as Int).toDouble()
                    } else if (geoAltitude == null) {
                        geoAltitude = 0.0
                    }

                    val newAircraft = Aircraft(
                        states[0] as String,
                        states[1] as String?,
                        states[2] as String,
                        states[4] as Int?,
                        states[5] as Double?,
                        states[6] as Double?,
                        baroAltitude as Double?,
                        states[8] as Boolean,
                        states[9] as Double?,
                        vertRate as Double?,
                        geoAltitude as Double?,
                        states[16] as Int
                    )

                    Log.e("Aircraft data", newAircraft.toString())

                    selectedAircraftLiveData.value = newAircraft
                    searchAircraftFlights()
                    searchAircraftCurrentFlightDetail()
                }
            }
        }
    }

    private fun getAircraftRequestParams(live: Boolean): Map<String, String> {
        val params = HashMap<String, String>()
        params["icao24"] = aircraftICAO.value!!
        if (!live) {
            params["time"] = aircraftLastSeen.value!!.toString()
        }
        return params
    }

    //On cherche le vol en cours de cet avion ou bien le dernier vol
    private fun searchAircraftCurrentFlightDetail() {
        val baseUrl = "https://opensky-network.org/api/routes"

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                RequestsManager.getSuspended(baseUrl, getAircraftCurrentFlightDetailParams())
            }
            if (result == null) {
                Log.e("Request", "Empty flight route data.")
                if (isAircraftOnline.value == false) {
                    Log.i("Request", "Falling back on last flight data")
                    searchRoutesForAirports(aircraftEstDepartureAirport.value, aircraftEstArrivalAirport.value)
                }

            } else {
                val routes = JSONObject(result)["route"] as JSONArray?

                if (routes == null) {
                    Log.e("Request", "Route data for this flight is empty.")
                } else {
                    if (routes.length() == 2) {
                        Log.i("Routes", "We have a correct routing $routes")
                        val estDeparture = routes[0] as String
                        val estArrival = routes[1] as String

                        Log.i("Routes", "Departure $estDeparture, Arrival: $estArrival")
                        searchRoutesForAirports(estDeparture, estArrival)
                    }
                }

            }
        }

    }

    private fun getAircraftCurrentFlightDetailParams(): Map<String, String> {
        val params = HashMap<String, String>()
        params["callsign"] = aircraftCallSign.value!!
        return params
    }

    // On cherche enfin les derniers vols de cet avion sur 3 jours
    private fun searchAircraftFlights() {
        val baseUrl = "https://opensky-network.org/api/flights/aircraft"

        viewModelScope.launch {
            isLoadingLiveData.value = true
            val result = withContext(Dispatchers.IO) {
                RequestsManager.getSuspended(baseUrl, getAircraftFlightsRequestParams())
            }
            if (result == null) {
                Log.e("Request", "Empty request response")
            } else {
                val flightList = Utils.getFlightListFromString(result)
                Log.d("Aircraft Flight List", flightList.toString())
                aircraftFlightListLiveData.value = flightList
            }
            isLoadingLiveData.value = false
        }
    }

    private fun getAircraftFlightsRequestParams(): Map<String, String> {
        val params = HashMap<String, String>()
        params["icao24"] = aircraftICAO.value!!

        val currentTime = Calendar.getInstance().timeInMillis / 1000;

        val fakeCalendar = Calendar.getInstance()
        // 3 derniers jours
        fakeCalendar.add(Calendar.DAY_OF_YEAR, -3)
        val threeDaysBefore = fakeCalendar.timeInMillis / 1000;

        params["begin"] = threeDaysBefore.toString()
        params["end"] = currentTime.toString()

        return params
    }



    fun updateSelectedFlightDataAndSearch(icao: String, callSign: String, lastSeen: Long, depAirport: String?, arrAirport: String?) {
        aircraftICAO.value = icao
        aircraftCallSign.value = callSign
        aircraftLastSeen.value = lastSeen
        aircraftEstDepartureAirport.value = depAirport
        aircraftEstArrivalAirport.value = arrAirport
        isAircraftOnline.value = true
        searchAircraftData(true)
    }

    fun getCurrentAircraftData(): LiveData<Aircraft> {
        return selectedAircraftLiveData
    }

    fun isAircraftOnline(): Boolean {
        if (isAircraftOnline.value != null) {
            return isAircraftOnline.value!!
        }
        return false
    }


    // Getting a airport detail

    private fun searchRoutesForAirports(dep: String?, arr: String?) {

        // We don't manage any missing airport data case
        if (dep == null || arr == null) {
            return
        }

        airportListLiveData.value!!.forEach {
            if (it.icao == dep) {
                Log.i("Airport finder", "Found departure airport coordinates")
                routeDepartureAirportCoords.value = LatLng(it.lat.toDouble(), it.lon.toDouble())
            }
        }

        if (routeDepartureAirportCoords.value == null) { return }

        val baseUrl = "https://opensky-network.org/api/airports"
        val params = HashMap<String, String>()
        params["icao"] = dep

        viewModelScope.launch {
            var result = withContext(Dispatchers.IO) {
                RequestsManager.getSuspended(baseUrl, params)
            }
            if (result == null) {
                Log.e("Airport finder", "Could not find a airport with this ICAO")
            } else {
                val position = JSONObject(result)["position"] as JSONObject
                val lat = position["latitude"] as Double
                val lon = position["longitude"] as Double
                Log.i("Airport finder", "Found departure airport coordinates")
                routeDepartureAirportCoords.value = LatLng(lat, lon)

                airportListLiveData.value!!.forEach {
                    if (it.icao == arr) {
                        Log.i("Airport finder", "Found arrival airport coordinates")
                        routeArrivalAirportCoords.value = LatLng(it.lat.toDouble(), it.lon.toDouble())
                        updateRouteCoords()
                    }
                }
                if (routeArrivalAirportCoords.value == null) {
                    val baseUrl = "https://opensky-network.org/api/airports"
                    val params = HashMap<String, String>()
                    params["icao"] = arr

                    viewModelScope.launch {
                        var result = withContext(Dispatchers.IO) {
                            RequestsManager.getSuspended(baseUrl, params)
                        }
                        if (result == null) {
                            Log.e("Airport finder", "Could not find a airport with this ICAO")
                        } else {
                            val position = JSONObject(result)["position"] as JSONObject
                            val lat = position["latitude"] as Double
                            val lon = position["longitude"] as Double
                            Log.i("Airport finder", "Found arrival airport coordinates")
                            routeArrivalAirportCoords.value = LatLng(lat, lon)
                            updateRouteCoords()
                        }
                    }
                }

            }
        }
    }

    private fun updateRouteCoords() {
        val coords = HashMap<String, LatLng>()
        coords["departure"] = routeDepartureAirportCoords.value!!
        coords["arrival"] = routeArrivalAirportCoords.value!!
        selectedFlightRouteCoordinates.value = coords
        Log.i("Airport finder", "Found final coordinates for both dep and arr airports ${selectedFlightRouteCoordinates.value}")
    }



}