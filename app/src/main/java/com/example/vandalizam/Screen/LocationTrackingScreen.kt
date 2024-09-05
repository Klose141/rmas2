package com.example.vandalizam

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationTrackingScreen(viewModel: LocationViewModel = viewModel()) {
    val context = LocalContext.current
    val locationPermissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)

    when {
        locationPermissionState.status.isGranted -> {
            // Lokacija je omogućena, prikazujemo mapu
            LocationMap(viewModel)
        }
        locationPermissionState.status.shouldShowRationale -> {
            // Prikaz poruke korisniku zašto je dozvola potrebna
            Text("Molimo omogućite pristup lokaciji da bi aplikacija mogla pratiti vašu lokaciju.")
        }
        else -> {
            // Zatraži dozvolu za pristup lokaciji
            LaunchedEffect(Unit) {
                locationPermissionState.launchPermissionRequest()
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun LocationMap(viewModel: LocationViewModel) {
    val cameraPositionState = rememberCameraPositionState()
    val markerState = rememberMarkerState()
    val context = LocalContext.current

    val currentLocation by viewModel.currentLocation.collectAsState() // Pratimo stateflow iz ViewModel-a

    // Koristimo FusedLocationProviderClient za dobijanje trenutne lokacije
    LaunchedEffect(Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                viewModel.updateLocation(LatLng(it.latitude, it.longitude)) // Ažuriramo ViewModel
                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15f))
            }
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        currentLocation?.let { location ->
            Marker(state = markerState.apply { position = location }, title = "Trenutna Lokacija")
        }
    }
}