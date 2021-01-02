package com.example.flightstatsm2loris.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flightstatsm2loris.network.RequestsManager
import com.example.flightstatsm2loris.utils.Utils
import com.example.flightstatsm2loris.models.Airport
import com.example.flightstatsm2loris.models.FlightModel
import com.example.flightstatsm2loris.models.SearchDataModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Created by sergio on 19/11/2020
 * All rights reserved GoodBarber
 */
class FlightListViewModel : ViewModel(), RequestsManager.RequestListener {

    private val airportListLiveData : MutableLiveData<List<Airport>> = MutableLiveData()
    val flightListLiveData: MutableLiveData<List<FlightModel>> = MutableLiveData()
    val isLoadingLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private val selectedFlightLiveData: MutableLiveData<FlightModel> = MutableLiveData()

    private val selectedFlightAirportsCoordinates: MutableLiveData<HashMap<String, LatLng?>> = MutableLiveData()

    init {
        airportListLiveData.value = Utils.generateAirportList()
    }

    //Si on trouve pas l'airport dans le json hardcodé on va le chercher sur l'API
    private fun retrieveAirportsCoordinates(flight: FlightModel) {
        var airportsCoords = HashMap<String, LatLng?>()

        var departureCoords = getDepartureAirportCoordinates()
        var arrivalCoords = getArrivalAirportCoordinates()
        if (departureCoords == null || arrivalCoords == null) {
            val baseUrl = "https://opensky-network.org/api/airports"
            val params = HashMap<String, String>()
            if (departureCoords == null) {
                params["icao"] = flight.estDepartureAirport
            } else if (arrivalCoords == null) {
                params["icao"] = flight.estArrivalAirport
            }
            viewModelScope.launch {
                var result = withContext(Dispatchers.IO) {
                    RequestsManager.getSuspended(baseUrl, params)
                }
                if (result == null) {
                    Log.e("Airport finder", "Could not find a airport with this ICAO")
                    airportsCoords["arrival"] = arrivalCoords
                    airportsCoords["departure"] = departureCoords
                    selectedFlightAirportsCoordinates.value = airportsCoords
                } else {
                    val position = JSONObject(result)["position"] as JSONObject
                    val lat = position["latitude"] as Double
                    val lon = position["longitude"] as Double
                    if (departureCoords == null) {
                        airportsCoords["arrival"] = arrivalCoords!!
                        airportsCoords["departure"] = LatLng(lat, lon)
                    } else if (arrivalCoords == null) {
                        airportsCoords["arrival"] = LatLng(lat, lon)
                        airportsCoords["departure"] = departureCoords!!
                    }
                    selectedFlightAirportsCoordinates.value = airportsCoords
                }
            }
        } else {
            airportsCoords["departure"] = departureCoords
            airportsCoords["arrival"] = arrivalCoords
            selectedFlightAirportsCoordinates.value = airportsCoords
        }
    }

    fun getSelectedFlightLiveData(): LiveData<FlightModel> {
        return selectedFlightLiveData
    }

    fun getAirportsDetailLiveData(): LiveData<HashMap<String, LatLng?>> {
        return selectedFlightAirportsCoordinates
    }

    private fun getDepartureAirportCoordinates(): LatLng? {
        var coordinates: LatLng? = null
        val dep = selectedFlightLiveData.value!!.estDepartureAirport
        airportListLiveData.value!!.forEach {
            if (it.icao == dep) {
                coordinates = LatLng(it.lat.toDouble(), it.lon.toDouble())
                return coordinates
            }
        }
        return coordinates
    }

    private fun getArrivalAirportCoordinates(): LatLng? {
        var coordinates: LatLng? = null
        val arr = selectedFlightLiveData.value!!.estArrivalAirport
        airportListLiveData.value!!.forEach {
            if (it.icao == arr) {
                coordinates = LatLng(it.lat.toDouble(), it.lon.toDouble())
                return coordinates
            }
        }
        return coordinates
    }

    fun search(icao: String, isArrival: Boolean, begin: Long, end: Long) {

        val searchDataModel = SearchDataModel(
            isArrival,
            icao,
            begin,
            end
        )
        val baseUrl: String = if (isArrival) {
            "https://opensky-network.org/api/flights/arrival"
        } else {
            "https://opensky-network.org/api/flights/departure"
        }

        viewModelScope.launch {
            //start loading
            isLoadingLiveData.value = true
            val result = withContext(Dispatchers.IO) {
                RequestsManager.getSuspended(baseUrl, getRequestParams(searchDataModel))
            }
            if (result == null) {
                Log.e("Request", "Empty request response")

            } else {
                val flightList = Utils.getFlightListFromString(result)
                Log.d("models list", flightList.toString())
                flightListLiveData.value = flightList
            }
            //end loading
            // we want to change the value here to make it more easier to display an error
            // message about the data being null or not
            isLoadingLiveData.value = false

        }
        // SearchFlightsAsyncTask(this).execute(searchDataModel)
    }



    private fun getRequestParams(searchModel: SearchDataModel?): Map<String, String>? {
        val params = HashMap<String, String>()
        if (searchModel != null) {
            params["airport"] = searchModel.icao
            params["begin"] = searchModel.begin.toString()
            params["end"] = searchModel.end.toString()
        }
        return params
    }

    override fun onRequestSuccess(result: String?) {
        TODO("Not yet implemented")
    }

    override fun onRequestFailed() {
        TODO("Not yet implemented")
    }

    fun updateSelectedFlight(flight: FlightModel) {
        selectedFlightLiveData.value = flight
        retrieveAirportsCoordinates(selectedFlightLiveData.value!!)
    }
}