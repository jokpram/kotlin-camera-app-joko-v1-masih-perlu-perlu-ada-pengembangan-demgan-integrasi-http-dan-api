package com.joko.cameraapp.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat

class LocationUtils(private val context: Context) {

    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null

    fun startLocationUpdates(onUpdate: (Location) -> Unit) {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                onUpdate(location)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        try {
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                1f,
                locationListener!!,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    fun stopLocationUpdates() {
        locationListener?.let {
            locationManager?.removeUpdates(it)
        }
    }

    fun getLastKnownLocation(): Location? {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }
        return try {
            locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        } catch (e: SecurityException) {
            null
        }
    }
}