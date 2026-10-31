package com.example.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class LocationStreamingManager(private val context: Context) {
    private val TAG = "LocationStreamingManager"

    // Default coordinates: Collesalvetti, Tuscany, Italy
    private val defaultLat = 43.5933
    private val defaultLng = 10.4735

    private val _currentLocation = MutableStateFlow<Pair<Double, Double>>(Pair(defaultLat, defaultLng))
    val currentLocation: StateFlow<Pair<Double, Double>> = _currentLocation.asStateFlow()

    private val _locationAddress = MutableStateFlow("Collesalvetti, Livorno, Italy")
    val locationAddress: StateFlow<String> = _locationAddress.asStateFlow()

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val lat = location.latitude
            val lng = location.longitude
            Log.i(TAG, "GPS location updated: $lat, $lng")
            _currentLocation.value = Pair(lat, lng)
            resolveAddress(lat, lng)
            FirebaseRiderService.updateLiveLocation(lat, lng)
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private fun resolveAddress(lat: Double, lng: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val geocoder = Geocoder(context, Locale.ITALY)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(lat, lng, 1) { addresses ->
                        if (addresses.isNotEmpty()) {
                            val addr = addresses[0]
                            val street = addr.thoroughfare ?: ""
                            val number = addr.subThoroughfare ?: ""
                            val city = addr.locality ?: addr.subAdminArea ?: "Collesalvetti"
                            val formatted = listOf(listOf(street, number).filter { it.isNotBlank() }.joinToString(" "), city, "Italy")
                                .filter { it.isNotBlank() }.joinToString(", ")
                            _locationAddress.value = formatted.ifBlank { "Collesalvetti, Livorno, Italy" }
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val list = geocoder.getFromLocation(lat, lng, 1)
                    if (!list.isNullOrEmpty()) {
                        val addr = list[0]
                        val street = addr.thoroughfare ?: ""
                        val city = addr.locality ?: addr.subAdminArea ?: "Collesalvetti"
                        _locationAddress.value = "$street, $city, Italy".trim().removePrefix(", ")
                    }
                }
            } catch (e: Exception) {
                _locationAddress.value = "Lat: %.4f, Lng: %.4f (Collesalvetti, Italy)".format(lat, lng)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        try {
            val hasGps = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
            val hasNetwork = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ?: false

            if (hasGps) {
                locationManager?.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    3000L,
                    5f,
                    locationListener
                )
                val lastGps = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastGps != null) {
                    _currentLocation.value = Pair(lastGps.latitude, lastGps.longitude)
                    resolveAddress(lastGps.latitude, lastGps.longitude)
                    FirebaseRiderService.updateLiveLocation(lastGps.latitude, lastGps.longitude)
                }
            } else if (hasNetwork) {
                locationManager?.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    5000L,
                    10f,
                    locationListener
                )
                val lastNet = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (lastNet != null) {
                    _currentLocation.value = Pair(lastNet.latitude, lastNet.longitude)
                    resolveAddress(lastNet.latitude, lastNet.longitude)
                    FirebaseRiderService.updateLiveLocation(lastNet.latitude, lastNet.longitude)
                }
            } else {
                Log.w(TAG, "No GPS or Network location provider available. Using default coordinates.")
                FirebaseRiderService.updateLiveLocation(defaultLat, defaultLng)
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Location permission not granted: ${e.message}")
        } catch (e: Throwable) {
            Log.e(TAG, "Error starting location updates: ${e.message}", e)
        }
    }

    fun stopLocationUpdates() {
        try {
            locationManager?.removeUpdates(locationListener)
        } catch (e: Exception) {
            Log.w(TAG, "Error removing location updates: ${e.message}")
        }
    }
}
