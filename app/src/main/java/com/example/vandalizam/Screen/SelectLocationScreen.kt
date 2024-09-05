package com.example.vandalizam

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState


private const val LOCATION_PERMISSION_REQUEST_CODE = 1

@Composable
fun SelectLocationScreen(onLocationSelected: (LatLng) -> Unit) {
    val context = LocalContext.current
    val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
    var hasPermission by remember { mutableStateOf(false) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }

    // Stanje kamere
    val cameraPositionState = rememberCameraPositionState()

    // Provera i traženje dozvole pre nego što se otvori mapa
    LaunchedEffect(Unit) {
        hasPermission = ContextCompat.checkSelfPermission(context, locationPermission) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            ActivityCompat.requestPermissions(
                context as ComponentActivity,
                arrayOf(locationPermission),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Ako je dozvola već odobrena, postavi trenutnu lokaciju
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    currentLocation = LatLng(it.latitude, it.longitude)
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(currentLocation!!, 15f))
                }
            }
        }
    }

    if (hasPermission) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Prikaz mape i markera
            GoogleMap(
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    // Privremeno skladištimo izabranu lokaciju
                    currentLocation = latLng
                },
                modifier = Modifier.weight(1f)
            ) {
                selectedLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Izabrana lokacija"
                    )
                }
                currentLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Trenutni izbor"
                    )
                }
            }

            // Dugme za potvrdu izbora lokacije
            Button(
                onClick = {
                    selectedLocation = currentLocation
                    selectedLocation?.let { onLocationSelected(it) }
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text(text = "Potvrdi lokaciju")
            }
        }
    } else {
        Text(text = "Aplikacija zahteva dozvolu za pristup lokaciji kako bi prikazala mapu.")
    }
}

class SelectLocationViewModel : ViewModel() {
    // ViewModel bi mogao biti proširen sa logikom za kasniju upotrebu
}