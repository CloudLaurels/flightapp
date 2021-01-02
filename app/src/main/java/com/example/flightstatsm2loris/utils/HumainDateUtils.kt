package com.example.flightstatsm2loris.utils

import java.util.*


/*object HumanDateUtils {
    fun durationFromNow(startDate: Int): String {
        var different = System.currentTimeMillis() - Date(startDate.toLong()).time
        val secondsInMilli: Long = 1000
        val minutesInMilli = secondsInMilli * 60
        val hoursInMilli = minutesInMilli * 60
        val daysInMilli = hoursInMilli * 24
        val elapsedDays = different / daysInMilli
        different = different % daysInMilli
        val elapsedHours = different / hoursInMilli
        different = different % hoursInMilli
        val elapsedMinutes = different / minutesInMilli
        different = different % minutesInMilli
        val elapsedSeconds = different / secondsInMilli
        var output = ""
        if (elapsedDays > 0) output += elapsedDays.toString() + "days "
        if (elapsedDays > 0 || elapsedHours > 0) output += "$elapsedHours hours "
        if (elapsedHours > 0 || elapsedMinutes > 0) output += "$elapsedMinutes minutes "
        if (elapsedMinutes > 0 || elapsedSeconds > 0) output += "$elapsedSeconds seconds"
        return output
    }
}*/

object TimeAgo {
    private const val SECOND_MILLIS = 1000
    private val MINUTE_MILLIS: Int = 60 * TimeAgo.SECOND_MILLIS
    private val HOUR_MILLIS: Int = 60 * TimeAgo.MINUTE_MILLIS
    private val DAY_MILLIS: Int = 24 * TimeAgo.HOUR_MILLIS
    fun getTimeAgo(time: Long): String? {
        var time = time
        if (time < 1000000000000L) {
            time *= 1000
        }
        val now = System.currentTimeMillis()
        if (time > now || time <= 0) {
            return null
        }
        val diff = now - time
        return if (diff < TimeAgo.MINUTE_MILLIS) {
            "just now"
        } else if (diff < 2 * TimeAgo.MINUTE_MILLIS) {
            "a minute ago"
        } else if (diff < 50 * TimeAgo.MINUTE_MILLIS) {
            (diff / TimeAgo.MINUTE_MILLIS).toString() + " minutes ago"
        } else if (diff < 90 * TimeAgo.MINUTE_MILLIS) {
            "an hour ago"
        } else if (diff < 24 * TimeAgo.HOUR_MILLIS) {
            (diff / TimeAgo.HOUR_MILLIS).toString() + " hours ago"
        } else if (diff < 48 * TimeAgo.HOUR_MILLIS) {
            "yesterday"
        } else {
            (diff / TimeAgo.DAY_MILLIS).toString() + " days ago"
        }
    }
}