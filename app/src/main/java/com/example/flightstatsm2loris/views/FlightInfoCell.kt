package com.example.flightstatsm2loris.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.example.flightstatsm2loris.R
import com.example.flightstatsm2loris.utils.Utils
import com.example.flightstatsm2loris.models.FlightModel

/**
 * Created by sergio on 15/12/2020
 * All rights reserved GoodBarber
 */
class FlightInfoCell : LinearLayout {

    var flightName: TextView? = null
        private set
    var depAirportName: TextView? = null
        private set
    var arrAirportName: TextView? = null
        private set
    var flightTime: TextView? = null
        private set
    var depDate: TextView? = null
        private set
    var arrDate: TextView? = null
        private set
    var depHour: TextView? = null
        private set
    var arrHour: TextView? = null
        private set


    constructor(context: Context?) : super(context) {
        initLayout()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initLayout()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initLayout()
    }

    private fun bindViews() {
        flightName = findViewById(R.id.flight_name)
        depAirportName = findViewById(R.id.departure_airport)
        arrAirportName = findViewById(R.id.arrival_airport)
        flightTime = findViewById(R.id.flight_duration)
        depDate = findViewById(R.id.departure_date)
        arrDate = findViewById(R.id.arrival_date)
        depHour = findViewById(R.id.departure_hour)
        arrHour = findViewById(R.id.arrival_hour)
    }

    //CACA
    fun bindData(flight : FlightModel){
        flightName?.text = "#"+ flight.callsign
        depAirportName?.text = flight.estDepartureAirport
        arrAirportName?.text = flight.estArrivalAirport
        depDate?.text = Utils.timestampToString(flight.firstSeen * 1000L)
        arrDate?.text = Utils.timestampToString(flight.lastSeen * 1000L)
        depHour?.text = Utils.timestampToHourMinute(flight.firstSeen * 1000L)
        arrHour?.text = Utils.timestampToHourMinute(flight.lastSeen * 1000L)
        flightTime?.text = Utils.formatFlightDuration(flight.lastSeen - flight.firstSeen)
    }

    private fun initLayout() {
        LayoutInflater.from(context).inflate(R.layout.flight_cell, this, true)
        bindViews()
    }

}