package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import com.example.data.firebase.FirebaseRiderService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class RiderLocationState(
    val latitude: Double = 43.5898,
    val longitude: Double = 10.4764,
    val heading: Float = 45f,
    val speedKmh: Float = 28f,
    val isStreaming: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

class LocationStreamingManager(
    private val context: Context,
    private val scope: CoroutineScope,
    private val firebaseService: FirebaseRiderService
) {
    private val tag = "LocationStreamingMgr"
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val _locationState = MutableStateFlow(RiderLocationState())
    val locationState: StateFlow<RiderLocationState> = _locationState.asStateFlow()

    private var streamingJob: Job? = null
    private var locationListener: LocationListener? = null

    private var currentRiderId: String = ""
    private var currentOrderId: String? = null

    private var lastKnownLocation: Location? = null
    private var simulatedProgress: Float = 0.0f

    private var continuousListener: LocationListener? = null

    init {
        // Fetch current location on startup and start tracking if permission available
        refreshCurrentLocation()
        startContinuousTracking()
    }

    @SuppressLint("MissingPermission")
    fun startContinuousTracking(riderId: String = "") {
        if (riderId.isNotBlank()) {
            currentRiderId = riderId
        }
        try {
            if (continuousListener == null) {
                continuousListener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        lastKnownLocation = location
                        _locationState.value = _locationState.value.copy(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            heading = location.bearing,
                            speedKmh = if (location.speed > 0) location.speed * 3.6f else 0f,
                            timestamp = System.currentTimeMillis()
                        )

                        // Stream to Firestore if rider is registered
                        if (currentRiderId.isNotBlank()) {
                            firebaseService.streamRiderLocation(
                                riderId = currentRiderId,
                                orderId = currentOrderId,
                                latitude = location.latitude,
                                longitude = location.longitude,
                                heading = location.bearing,
                                speedKmh = if (location.speed > 0) location.speed * 3.6f else 0f
                            )
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }

                continuousListener?.let { listener ->
                    if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 1f, listener)
                    }
                    if (locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000L, 2f, listener)
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.w(tag, "Location permission not yet granted: ${e.message}")
        } catch (e: Throwable) {
            Log.w(tag, "Continuous tracking setup warning: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun refreshCurrentLocation() {
        try {
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )
            for (provider in providers) {
                if (locationManager?.isProviderEnabled(provider) == true) {
                    val loc = locationManager.getLastKnownLocation(provider)
                    if (loc != null) {
                        lastKnownLocation = loc
                        _locationState.value = _locationState.value.copy(
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            heading = loc.bearing,
                            speedKmh = if (loc.speed > 0) loc.speed * 3.6f else 0f,
                            timestamp = System.currentTimeMillis()
                        )
                        Log.d(tag, "Real device location updated from $provider: ${loc.latitude}, ${loc.longitude}")
                        break
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.w(tag, "Location permission not yet granted: ${e.message}")
        } catch (e: Throwable) {
            Log.w(tag, "Error refreshing location: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun startStreaming(
        riderId: String,
        orderId: String?,
        fromLat: Double = 43.5898,
        fromLng: Double = 10.4764,
        toLat: Double = 43.5800,
        toLng: Double = 10.4850
    ) {
        currentRiderId = riderId
        currentOrderId = orderId
        simulatedProgress = 0.05f

        // Try getting initial device location
        try {
            val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            for (provider in providers) {
                if (locationManager?.isProviderEnabled(provider) == true) {
                    val loc = locationManager.getLastKnownLocation(provider)
                    if (loc != null) {
                        lastKnownLocation = loc
                        break
                    }
                }
            }

            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    lastKnownLocation = location
                    _locationState.value = _locationState.value.copy(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        heading = location.bearing,
                        speedKmh = if (location.speed > 0) location.speed * 3.6f else 28.5f,
                        timestamp = System.currentTimeMillis()
                    )
                }
                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            locationListener?.let { listener ->
                if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        2000L,
                        2f,
                        listener
                    )
                }
                if (locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        3000L,
                        5f,
                        listener
                    )
                }
            }
        } catch (e: SecurityException) {
            Log.w(tag, "Location permission not granted, falling back to simulated GPS stream: ${e.message}")
        } catch (e: Exception) {
            Log.w(tag, "Location manager setup warning: ${e.message}")
        }

        streamingJob?.cancel()
        streamingJob = scope.launch(Dispatchers.IO) {
            _locationState.value = _locationState.value.copy(isStreaming = true)
            Log.d(tag, "Started GPS location streaming for order $orderId to riders/$riderId/location")

            while (isActive) {
                // If real device location exists, use it; otherwise smoothly interpolate between from and to coords
                val (lat, lng, heading, speed) = if (lastKnownLocation != null && lastKnownLocation!!.latitude != 0.0) {
                    val loc = lastKnownLocation!!
                    Quadruple(
                        loc.latitude,
                        loc.longitude,
                        loc.bearing,
                        if (loc.speed > 0) loc.speed * 3.6f else 28.5f
                    )
                } else {
                    // Simulated realistic route movement along Dhaka streets
                    simulatedProgress = (simulatedProgress + 0.04f).coerceAtMost(0.98f)
                    val currLat = fromLat + (toLat - fromLat) * simulatedProgress + ((1..5).random() - 3) * 0.00005
                    val currLng = fromLng + (toLng - fromLng) * simulatedProgress + ((1..5).random() - 3) * 0.00005
                    Quadruple(currLat, currLng, 135f, 29.0f + (0..5).random())
                }

                _locationState.value = RiderLocationState(
                    latitude = lat,
                    longitude = lng,
                    heading = heading,
                    speedKmh = speed,
                    isStreaming = true,
                    timestamp = System.currentTimeMillis()
                )

                // Stream to Firestore
                firebaseService.streamRiderLocation(
                    riderId = currentRiderId,
                    orderId = currentOrderId,
                    latitude = lat,
                    longitude = lng,
                    heading = heading,
                    speedKmh = speed
                )

                delay(3000L) // 3-second interval
            }
        }
    }

    fun stopStreaming() {
        try {
            locationListener?.let { locationManager?.removeUpdates(it) }
        } catch (e: Exception) {
            Log.w(tag, "Error removing location listener: ${e.message}")
        } finally {
            locationListener = null
        }

        streamingJob?.cancel()
        streamingJob = null
        _locationState.value = _locationState.value.copy(isStreaming = false)
        Log.d(tag, "Stopped GPS location streaming")
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
