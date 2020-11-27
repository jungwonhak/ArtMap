package us.ait.artlocater

import android.content.Context
import android.os.Looper
import com.google.android.gms.location.*

class MyLocationManager(context: Context) {

    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @Throws(SecurityException::class)
    fun startLocationMonitoring() {
        val locationRequest = LocationRequest()
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 500
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun stopLocationMonitoring() {
        fusedLocationClient.removeLocationUpdates(locationCallback)

    }

    private var locationCallback: LocationCallback =
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                (context as MapsActivity).showLocation(locationResult!!.lastLocation)
            }
        }
}