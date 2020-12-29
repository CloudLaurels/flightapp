package com.example.flightstatsm2loris.models

// To parse the JSON, install Klaxon and do:
//
//   val welcome9 = Welcome9.fromJson(jsonString)


import com.beust.klaxon.*

data class Aircraft(
    val icao: String,
    val callsign: String?,
    val originCountry: String,
    val lastContact: Int?,
    val longitude: Double?,
    val latitude: Double?,
    val baroAltitude: Double?,
    val onGround: Boolean,
    val velocity: Double?,
    val verticalRate: Double?,
    val geoAltitude: Double?,
    val positionSource: Int
)
