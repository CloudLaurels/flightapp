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

/**
 * Created by sergio on 19/11/2020
 * All rights reserved GoodBarber
 */
class FlightListViewModel : ViewModel(), RequestsManager.RequestListener {

    private val airportListLiveData : MutableLiveData<List<Airport>> = MutableLiveData()
    val flightListLiveData: MutableLiveData<List<FlightModel>> = MutableLiveData()
    val isLoadingLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private val selectedFlightLiveData: MutableLiveData<FlightModel> = MutableLiveData()

    init {
        airportListLiveData.value = Utils.generateAirportList()
    }

    fun getSelectedFlightLiveData(): LiveData<FlightModel> {
        return selectedFlightLiveData
    }

    fun getDepartureAirportCoordinates(): LatLng {
        var coordinates = LatLng(0.0, 0.0)
        val dep = selectedFlightLiveData.value!!.estDepartureAirport
        airportListLiveData.value!!.forEach {
            if (it.icao == dep) {
                coordinates = LatLng(it.lat.toDouble(), it.lon.toDouble())
                return coordinates
            }
        }
        return coordinates
    }

    fun getArrivalAirportCoordinates(): LatLng {
        var coordinates = LatLng(0.0, 0.0)
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
            //end loading
            isLoadingLiveData.value = false
            if (result == null) {
                Log.e("Request", "Empty request response")

            } else {
                val flightList = Utils.getFlightListFromString(result)
                Log.d("models list", flightList.toString())
                flightListLiveData.value = flightList
            }

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
    }
}