package com.example.flightstatsm2loris.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flightstatsm2loris.models.Aircraft
import com.example.flightstatsm2loris.models.FlightModel
import com.example.flightstatsm2loris.network.RequestsManager
import com.example.flightstatsm2loris.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

class AircraftDetailViewModel : ViewModel(), RequestsManager.RequestListener {

    val isLoadingLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private val selectedAircraftLiveData: MutableLiveData<Aircraft> = MutableLiveData()

    val aircraftICAO: MutableLiveData<String> = MutableLiveData()
    val aircraftLastSeen: MutableLiveData<Long> = MutableLiveData()
    val aircraftEstDepartureAirport: MutableLiveData<String> = MutableLiveData()
    val aircraftEstArrivalAirport: MutableLiveData<String> = MutableLiveData()

    val isAircraftOnline: MutableLiveData<Boolean> = MutableLiveData()

    val aircraftFlightListLiveData: MutableLiveData<List<FlightModel>> = MutableLiveData()




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
                    Log.e("Parsing aircraft data", "States are null, we should specify a time, now trying with last time seen")
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

    private fun searchAircraftFlightDetail() {

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



    fun updateSelectedFlightDataAndSearch(icao: String, lastSeen: Long, depAirport: String?, arrAirport: String?) {
        aircraftICAO.value = icao
        aircraftLastSeen.value = lastSeen
        aircraftEstDepartureAirport.value = depAirport
        aircraftEstArrivalAirport.value = arrAirport
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

}