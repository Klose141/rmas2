package com.example.vandalizam

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.*

@Composable
fun IncidentDetailScreen(
    incidentId: String,
    viewModel: IncidentDetailViewModel = viewModel(),
    onBackPressed: () -> Unit
) {
    val incident = viewModel.incident.value

    LaunchedEffect(incidentId) {
        viewModel.loadIncidentDetails(incidentId)
    }

    incident?.let {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(text = "Detalji incidenta", modifier = Modifier.padding(bottom = 16.dp))

            Text(text = "Naslov: ${it.title}")
            Text(text = "Opis: ${it.description}")

            Spacer(modifier = Modifier.height(16.dp))

            GoogleMap(
                modifier = Modifier
                    .fillMaxHeight(0.5f)
                    .padding(bottom = 16.dp),
                cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(LatLng(it.latitude, it.longitude), 15f)
                }
            ) {
                Marker(
                    state = MarkerState(position = LatLng(it.latitude, it.longitude)),
                    title = it.title
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onBackPressed) {
                Text("Nazad")
            }
        }
    } ?: run {
        Text(text = "Učitavanje podataka o incidentu...", modifier = Modifier.padding(16.dp))
    }
}

class IncidentDetailViewModel : ViewModel() {
    private val _incident = mutableStateOf<Incident?>(null)
    val incident: State<Incident?> = _incident

    fun loadIncidentDetails(incidentId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("incidents").document(incidentId)
            .get()
            .addOnSuccessListener { document ->
                val title = document.getString("title") ?: ""
                val description = document.getString("description") ?: ""
                val latitude = document.getDouble("latitude") ?: 0.0
                val longitude = document.getDouble("longitude") ?: 0.0

                // Dodajemo id uz ostale podatke
                _incident.value = Incident(incidentId, title, description, latitude, longitude)
            }
    }
}