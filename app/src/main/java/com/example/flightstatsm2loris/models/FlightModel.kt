package com.example.flightstatsm2loris.models

/**
 * Created by sergio on 19/11/2020
 * All rights reserved GoodBarber
 */
data class FlightModel (val icao24: String,
                        val firstSeen: Long,
                        val estDepartureAirport: String,
                        val lastSeen: Long,
                        val estArrivalAirport: String,
                        val callsign: String,
                        val estDepartureAirportHorizDistance: Int,
                        val estDepartureAirportVertDistance: Int,
                        val estArrivalAirportHorizDistance: Int,
                        val estArrivalAirportVertDistance: Int,
                        val departureAirportCandidatesCount: Int,
                        val arrivalAirportCandidatesCount: Int)