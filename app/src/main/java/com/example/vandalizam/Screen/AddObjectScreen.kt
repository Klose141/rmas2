package com.example.vandalizam

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
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
fun AddObjectScreen(viewModel: LocationViewModel = viewModel()) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val currentLocation by viewModel.currentLocation.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        currentLocation?.let { location ->
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Naslov Incidenta") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Opis Incidenta") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Placeholder za dodavanje slike
            Button(onClick = { /* Kod za biranje slike */ }) {
                Text("Dodaj Sliku Incidenta")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    addObjectToFirestore(location, title, description, imageUri)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dodaj Incident")
            }
        } ?: run {
            Text("Lokacija nije dostupna")
        }

        Spacer(modifier = Modifier.height(16.dp))

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(currentLocation ?: LatLng(0.0, 0.0), 15f)
            }
        ) {
            currentLocation?.let { location ->
                Marker(state = MarkerState(position = location), title = "Trenutna Lokacija")
            }
        }
    }
}

fun addObjectToFirestore(
    location: LatLng,
    title: String,
    description: String,
    imageUri: Uri?
) {
    val db = FirebaseFirestore.getInstance()
    val incident = hashMapOf(
        "title" to title,
        "description" to description,
        "latitude" to location.latitude,
        "longitude" to location.longitude,
        "imageUri" to imageUri.toString()
    )

    db.collection("incidents")
        .add(incident)
        .addOnSuccessListener {
            // Uspešno dodavanje
        }
        .addOnFailureListener { e ->
            // Obrada greške
        }
}