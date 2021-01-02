package com.example.flightstatsm2loris.models

// To parse the JSON, install Klaxon and do:
//
//   val welcome9 = Welcome9.fromJson(jsonString)


import kotlin.math.round

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
    val positionSource: Int,
    val currentOrientation: Double?
) {
    // Je pense que ces fonctions devraient aller dans le view model
    // mais c'est plus pratique ici
    fun getLitteralState(): String {
        if (onGround) {
            return "Not flying"
        }
        return "Flying"
    }

    fun getLitteralVerticalRate(): String {
        if (verticalRate!!.compareTo(0.0) < 0) {
            return "$verticalRate (Downhill)"
        }
        return "$verticalRate (Uphill)"
    }

    fun getLitteralVelocity(): String {
        return "$velocity m/s"
    }

    fun getLitteralBaroAlt(): String {
        val lit = round(baroAltitude!!).toString()
        return "$lit m"
    }

    fun getLitteralGeoAlt(): String {
        val lit = round(geoAltitude!!).toString()
        return "$lit m"
    }

    fun getLitteralPositionSource(): String {
        when(positionSource) {
            0 -> return "ADS-B"
            1 -> return "ASTERIX"
            2 -> return "MLAT"
        }
        return positionSource.toString()
    }
}
