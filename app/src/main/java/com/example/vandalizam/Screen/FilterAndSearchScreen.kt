package com.example.vandalizam

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.*

@Composable
fun FilterAndSearchScreen(viewModel: LocationViewModel = viewModel()) {
    var searchQuery by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf(0.0) }
    val currentLocation by viewModel.currentLocation.collectAsState()
    val incidents = remember { mutableStateListOf<Incident>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Pretraži po naslovu ili opisu") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = distance.toString(),
            onValueChange = { distance = it.toDoubleOrNull() ?: 0.0 },
            label = { Text("Udaljenost u kilometrima") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (currentLocation != null) {
                searchIncidents(currentLocation!!, searchQuery, distance, incidents)
            }
        }) {
            Text("Pretraži")
        }

        Spacer(modifier = Modifier.height(16.dp))

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(currentLocation ?: LatLng(0.0, 0.0), 15f)
            }
        ) {
            incidents.forEach { incident ->
                Marker(
                    state = MarkerState(position = LatLng(incident.latitude, incident.longitude)),
                    title = incident.title,
                    snippet = incident.description
                )
            }
        }
    }
}



fun searchIncidents(
    currentLocation: LatLng,
    query: String,
    distance: Double,
    incidents: MutableList<Incident>
) {
    val db = FirebaseFirestore.getInstance()
    db.collection("incidents")
        .get()
        .addOnSuccessListener { result ->
            incidents.clear()
            for (document in result) {
                val id = document.id // Preuzimamo ID dokumenta iz Firestore-a
                val title = document.getString("title") ?: ""
                val description = document.getString("description") ?: ""
                val latitude = document.getDouble("latitude") ?: 0.0
                val longitude = document.getDouble("longitude") ?: 0.0
                val incidentLocation = LatLng(latitude, longitude)

                if (title.contains(query, true) || description.contains(query, true)) {
                    val distanceBetween = calculateDistance(currentLocation, incidentLocation)
                    if (distanceBetween <= distance) {
                        incidents.add(
                            Incident(
                                id = id,               // Dodajemo ID incidenta
                                title = title,
                                description = description,
                                latitude = latitude,
                                longitude = longitude
                            )
                        )
                    }
                }
            }
        }
}

fun calculateDistance(location1: LatLng, location2: LatLng): Double {
    val earthRadius = 6371.0 // Zemljin poluprečnik u kilometrima
    val dLat = Math.toRadians(location2.latitude - location1.latitude)
    val dLng = Math.toRadians(location2.longitude - location1.longitude)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(location1.latitude)) * Math.cos(Math.toRadians(location2.latitude)) *
            Math.sin(dLng / 2) * Math.sin(dLng / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return earthRadius * c
}