package com.google.location.nearby.apps.walkietalkies.utils

import android.content.Context
import android.location.LocationManager

object CommonFunctions {

    fun locationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

}