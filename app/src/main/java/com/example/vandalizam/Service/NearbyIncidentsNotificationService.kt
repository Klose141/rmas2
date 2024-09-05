package com.example.vandalizam

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore

class NearbyIncidentsNotificationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startForegroundService()
        startLocationUpdates()
    }

    private fun startForegroundService() {
        val channelId = "nearby_incidents_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Bliski Incidenti",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Vandalizam")
            .setContentText("Praćenje bliskih incidenata.")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .build()

        startForeground(1, notification)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // Ažuriranje lokacije svakih 10 sekundi
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val currentLocation = LatLng(location.latitude, location.longitude)
                    checkForNearbyIncidents(currentLocation)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun checkForNearbyIncidents(currentLocation: LatLng) {
        val db = FirebaseFirestore.getInstance()
        db.collection("incidents")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val latitude = document.getDouble("latitude") ?: 0.0
                    val longitude = document.getDouble("longitude") ?: 0.0
                    val incidentLocation = LatLng(latitude, longitude)

                    val distance = calculateDistance(currentLocation, incidentLocation)
                    if (distance <= 1.0) { // Ako je incident na udaljenosti manjoj od 1 km
                        sendIncidentNotification(document.getString("title") ?: "Incident u blizini")
                    }
                }
            }
    }

    private fun sendIncidentNotification(incidentTitle: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, "nearby_incidents_channel")
            .setContentTitle("Incident u blizini")
            .setContentText(incidentTitle)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(2, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun calculateDistance(location1: LatLng, location2: LatLng): Double {
        val earthRadius = 6371.0 // Zemljin poluprečnik u kilometrima
        val dLat = Math.toRadians(location2.latitude - location1.latitude)
        val dLng = Math.toRadians(location2.longitude - location1.longitude)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(location1.latitude)) * Math.cos(Math.toRadians(location2.latitude)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }
}