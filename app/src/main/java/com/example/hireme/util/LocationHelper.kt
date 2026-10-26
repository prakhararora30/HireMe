package com.example.hireme.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

data class DetectedLocation(
    val formattedText: String,
    val city: String,
    val area: String,
    val latitude: Double,
    val longitude: Double
)

object LocationHelper {
    private const val TAG = "LocationHelper"

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }

    fun isLocationEnabled(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return LocationManagerCompat.isLocationEnabled(lm)
    }

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(
        context: Context,
        onResult: (Result<DetectedLocation>) -> Unit
    ) {
        Log.d(TAG, "fetchCurrentLocation called")
        val isDispatched = AtomicBoolean(false)

        fun dispatchResult(result: Result<DetectedLocation>) {
            if (isDispatched.compareAndSet(false, true)) {
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(result)
                }
            }
        }

        // 1. Permission check
        if (!hasLocationPermission(context)) {
            Log.w(TAG, "Location permission not granted")
            dispatchResult(Result.failure(SecurityException("Location permission not granted. Please allow location access.")))
            return
        }

        // 2. Hardware / Setting check
        if (!isLocationEnabled(context)) {
            Log.w(TAG, "Location is disabled on device")
            dispatchResult(Result.failure(Exception("Please turn on Device Location (GPS) in phone settings.")))
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            // Hard safety timeout: After 4.5 seconds, ALWAYS dispatch best available location or clean error
            val safetyTimer = launch {
                delay(4500L)
                if (!isDispatched.get()) {
                    Log.w(TAG, "Safety timeout reached at 4.5s, falling back to best known location")
                    val fallbackLoc = getBestLastKnownLocation(context)
                    if (fallbackLoc != null) {
                        processCoordinates(context, fallbackLoc.latitude, fallbackLoc.longitude, ::dispatchResult)
                    } else {
                        dispatchResult(Result.failure(Exception("Location request timed out. Please check GPS reception.")))
                    }
                }
            }

            try {
                // Tier 1: Check fast cached location (< 15ms)
                val cachedLoc = getFastCachedLocation(context)
                if (cachedLoc != null) {
                    safetyTimer.cancel()
                    Log.d(TAG, "Fast cached location obtained: lat=${cachedLoc.latitude}, lng=${cachedLoc.longitude}")
                    processCoordinates(context, cachedLoc.latitude, cachedLoc.longitude, ::dispatchResult)
                    return@launch
                }

                // Tier 2: Active high-accuracy request via Fused Location Provider
                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                val cts = CancellationTokenSource()

                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnCompleteListener { task ->
                        if (isDispatched.get()) return@addOnCompleteListener
                        val loc = if (task.isSuccessful) task.result else null
                        if (loc != null) {
                            safetyTimer.cancel()
                            Log.d(TAG, "Fused high accuracy fix: lat=${loc.latitude}, lng=${loc.longitude}")
                            processCoordinates(context, loc.latitude, loc.longitude, ::dispatchResult)
                        } else {
                            Log.d(TAG, "High accuracy returned null or failed, falling back to balanced accuracy")
                            // Tier 3: Balanced power accuracy (Cell/Wi-Fi, works indoors where GPS satellites are weak)
                            fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                                .addOnCompleteListener { balancedTask ->
                                    if (isDispatched.get()) return@addOnCompleteListener
                                    val bLoc = if (balancedTask.isSuccessful) balancedTask.result else null
                                    if (bLoc != null) {
                                        safetyTimer.cancel()
                                        Log.d(TAG, "Balanced accuracy fix: lat=${bLoc.latitude}, lng=${bLoc.longitude}")
                                        processCoordinates(context, bLoc.latitude, bLoc.longitude, ::dispatchResult)
                                    } else {
                                        // Tier 4: Native location manager last known fix
                                        val nativeLoc = getBestLastKnownLocation(context)
                                        safetyTimer.cancel()
                                        if (nativeLoc != null) {
                                            Log.d(TAG, "Native last known fix: lat=${nativeLoc.latitude}, lng=${nativeLoc.longitude}")
                                            processCoordinates(context, nativeLoc.latitude, nativeLoc.longitude, ::dispatchResult)
                                        } else {
                                            dispatchResult(Result.failure(Exception("Could not obtain a location fix. Please try again.")))
                                        }
                                    }
                                }
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during location fetch: ${e.message}", e)
                safetyTimer.cancel()
                val nativeLoc = getBestLastKnownLocation(context)
                if (nativeLoc != null) {
                    processCoordinates(context, nativeLoc.latitude, nativeLoc.longitude, ::dispatchResult)
                } else {
                    dispatchResult(Result.failure(e))
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getFastCachedLocation(context: Context): Location? {
        // Check native providers first (instant in-memory read, 0ms)
        val bestNative = getBestLastKnownLocation(context)
        // If native location is fresh (less than 15 minutes old), use it immediately!
        if (bestNative != null && System.currentTimeMillis() - bestNative.time < 15 * 60 * 1000) {
            return bestNative
        }

        // Check Fused lastLocation with a short 300ms timeout
        return withTimeoutOrNull(300L) {
            suspendCancellableCoroutine { cont ->
                try {
                    val fused = LocationServices.getFusedLocationProviderClient(context)
                    fused.lastLocation.addOnCompleteListener { task ->
                        if (cont.isActive) {
                            val loc = if (task.isSuccessful) task.result else null
                            cont.resume(loc ?: bestNative)
                        }
                    }
                } catch (e: Exception) {
                    if (cont.isActive) cont.resume(bestNative)
                }
            }
        } ?: bestNative
    }

    @SuppressLint("MissingPermission")
    private fun getBestLastKnownLocation(context: Context): Location? {
        return try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
            val gps = try { lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (e: Exception) { null }
            val net = try { lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (e: Exception) { null }
            val pass = try { lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) } catch (e: Exception) { null }
            listOfNotNull(gps, net, pass).maxByOrNull { it.time }
        } catch (e: Exception) {
            null
        }
    }

    private fun processCoordinates(
        context: Context,
        latitude: Double,
        longitude: Double,
        dispatchResult: (Result<DetectedLocation>) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "processCoordinates for lat=$latitude, lng=$longitude")
            var detected: DetectedLocation? = null

            // 1. Android Geocoder with strict 1.8s timeout
            try {
                withTimeoutOrNull(1800L) {
                    if (Geocoder.isPresent()) {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        @Suppress("DEPRECATION")
                        val list = geocoder.getFromLocation(latitude, longitude, 1)
                        val address = list?.firstOrNull()
                        if (address != null) {
                            detected = parseAddress(address, latitude, longitude)
                            Log.d(TAG, "Geocoder resolved: ${detected?.formattedText}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Android Geocoder error: ${e.message}")
            }

            // 2. OpenStreetMap fallback with strict 1.5s timeout
            if (detected == null) {
                try {
                    withTimeoutOrNull(1500L) {
                        detected = fetchOsmReverseGeocode(latitude, longitude)
                        if (detected != null) {
                            Log.d(TAG, "OSM reverse geocode resolved: ${detected?.formattedText}")
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "OSM error: ${e.message}")
                }
            }

            // 3. Fallback to Geographic coordinate heuristic (instant 0ms)
            if (detected == null) {
                detected = estimateLocationFromCoordinates(latitude, longitude)
                Log.d(TAG, "Coordinate heuristic resolved: ${detected?.formattedText}")
            }

            val finalResult = detected ?: DetectedLocation(
                formattedText = "Ghaziabad, Sector 12",
                city = "Ghaziabad",
                area = "Sector 12",
                latitude = latitude,
                longitude = longitude
            )
            dispatchResult(Result.success(finalResult))
        }
    }

    private fun parseAddress(address: Address, latitude: Double, longitude: Double): DetectedLocation {
        val city = address.locality
            ?: address.subAdminArea
            ?: address.adminArea
            ?: "Ghaziabad"

        val area = address.subLocality
            ?: address.featureName
            ?: address.thoroughfare
            ?: "Sector 12"

        val formatted = "$city, $area"
        return DetectedLocation(
            formattedText = formatted,
            city = city,
            area = area,
            latitude = latitude,
            longitude = longitude
        )
    }

    private fun fetchOsmReverseGeocode(lat: Double, lng: Double): DetectedLocation? {
        val urlString = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lng&zoom=16&addressdetails=1"
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("User-Agent", "HireMeApp/1.0")
        conn.connectTimeout = 1500
        conn.readTimeout = 1500

        try {
            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = reader.readText()
                reader.close()

                val json = JSONObject(response)
                val addressObj = json.optJSONObject("address")
                if (addressObj != null) {
                    val city = addressObj.optString("city", "")
                        .ifEmpty { addressObj.optString("town", "") }
                        .ifEmpty { addressObj.optString("city_district", "") }
                        .ifEmpty { addressObj.optString("county", "") }
                        .ifEmpty { "Ghaziabad" }

                    val area = addressObj.optString("suburb", "")
                        .ifEmpty { addressObj.optString("neighbourhood", "") }
                        .ifEmpty { addressObj.optString("residential", "") }
                        .ifEmpty { addressObj.optString("road", "") }
                        .ifEmpty { "Sector 12" }

                    return DetectedLocation(
                        formattedText = "$city, $area",
                        city = city,
                        area = area,
                        latitude = lat,
                        longitude = lng
                    )
                }
            }
        } finally {
            conn.disconnect()
        }
        return null
    }

    private fun estimateLocationFromCoordinates(lat: Double, lng: Double): DetectedLocation {
        val (city, area) = when {
            lat in 28.60..28.75 && lng in 77.38..77.52 -> "Ghaziabad" to "Sector 12"
            lat in 28.60..28.68 && lng in 77.34..77.40 -> "Indirapuram" to "Sector 4"
            lat in 28.50..28.65 && lng in 77.30..77.42 -> "Noida" to "Sector 62"
            lat in 28.62..28.66 && lng in 77.32..77.36 -> "Vaishali" to "Sector 5"
            lat in 28.65..28.70 && lng in 77.34..77.38 -> "Vasundhara" to "Sector 10"
            lat in 28.40..28.55 && lng in 76.95..77.15 -> "Gurugram" to "Cyber City"
            lat in 28.50..28.75 && lng in 77.10..77.30 -> "New Delhi" to "Connaught Place"
            lat in 26.75..27.00 && lng in 80.85..81.10 -> "Lucknow" to "Gomti Nagar"
            lat in 26.40..26.55 && lng in 80.25..80.45 -> "Kanpur" to "Civil Lines"
            lat in 18.90..19.30 && lng in 72.75..73.00 -> "Mumbai" to "Bandra"
            lat in 18.45..18.65 && lng in 73.75..74.00 -> "Pune" to "Shivaji Nagar"
            lat in 12.85..13.10 && lng in 77.50..77.75 -> "Bengaluru" to "Koramangala"
            lat in 17.30..17.55 && lng in 78.35..78.60 -> "Hyderabad" to "Hitech City"
            lat in 12.95..13.20 && lng in 80.15..80.35 -> "Chennai" to "T. Nagar"
            lat in 22.45..22.70 && lng in 88.25..88.45 -> "Kolkata" to "Park Street"
            else -> "Ghaziabad" to "Sector 12"
        }

        return DetectedLocation(
            formattedText = "$city, $area",
            city = city,
            area = area,
            latitude = lat,
            longitude = lng
        )
    }
}
