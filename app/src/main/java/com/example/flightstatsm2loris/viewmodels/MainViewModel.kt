package com.example.flightstatsm2loris.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flightstatsm2loris.utils.Utils
import com.example.flightstatsm2loris.models.Airport
import java.util.*

/**
 * Created by sergio on 19/11/2020
 * All rights reserved GoodBarber
 */
class MainViewModel : ViewModel(){

    private val airportListLiveData : MutableLiveData<List<Airport>> = MutableLiveData()
    private val beginDateLiveData : MutableLiveData<Calendar> = MutableLiveData()
    private val endDateLiveData : MutableLiveData<Calendar> = MutableLiveData()

    init {
        airportListLiveData.value = Utils.generateAirportList()
        beginDateLiveData.value = Calendar.getInstance()
        endDateLiveData.value = Calendar.getInstance()
    }


    fun getAirportListLiveData(): LiveData<List<Airport>>{
        return airportListLiveData
    }

    fun getBeginDateLiveData(): LiveData<Calendar>{
        return beginDateLiveData
    }

    fun getEndDateLiveData(): LiveData<Calendar>{
        return endDateLiveData
    }

    fun updateBeginCalendar(year :Int, month: Int, day:Int){
        val calendar=Calendar.getInstance()
        calendar.set(year, month, day, 0, 0)
        beginDateLiveData.value=calendar
    }

    fun updateEndCalendar(year :Int, month: Int, day:Int){
        val calendar=Calendar.getInstance()
        calendar.set(year,month,day)
        endDateLiveData.value=calendar
    }



}