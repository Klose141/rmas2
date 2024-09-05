package com.example.vandalizam


import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

import java.util.*

@Composable
fun ReportIncidentScreen(
    onIncidentReported: () -> Unit,
    onSelectLocation: () -> Unit,
    initialLocation: LatLng?
) {
    var location by rememberSaveable { mutableStateOf(initialLocation) }
    var incidentDetails by rememberSaveable { mutableStateOf("") }
    var incidentDescription by rememberSaveable { mutableStateOf("") }

    Column {
        Button(onClick = { onSelectLocation() }) {
            Text(text = if (location != null) "Izabrana lokacija" else "Izaberi lokaciju")
        }

        // Polje za unos detalja incidenta
        TextField(
            value = incidentDetails,
            onValueChange = { incidentDetails = it },
            label = { Text("Detalji incidenta") }
        )

        // Polje za unos opisa incidenta
        TextField(
            value = incidentDescription,
            onValueChange = { incidentDescription = it },
            label = { Text("Opis incidenta") }
        )

        Button(onClick = {
            // Logika za prijavljivanje incidenta
            onIncidentReported()
        }) {
            Text("Prijavi incident")
        }
    }
}

class ReportIncidentViewModel : ViewModel() {

    fun reportIncident(
        title: String,
        description: String,
        location: LatLng?,
        imageUri: Uri?,
        onUploadComplete: (String?) -> Unit
    ) {
        if (location == null) return

        val db = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance().reference
        val incident = hashMapOf(
            "title" to title,
            "description" to description,
            "latitude" to location.latitude,
            "longitude" to location.longitude
        )

        // Ako postoji slika, prvo je upload-uj
        if (imageUri != null) {
            val imageRef = storage.child("incident_images/${UUID.randomUUID()}.jpg")
            imageRef.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()

                        // Dodaj incident sa URL-om slike u Firestore
                        incident["imageUri"] = imageUrl
                        db.collection("incidents")
                            .add(incident)
                            .addOnSuccessListener {
                                onUploadComplete(imageUrl)
                            }
                    }
                }
                .addOnFailureListener {
                    onUploadComplete(null) // U slučaju greške
                }
        } else {
            // Ako nema slike, samo dodaj incident bez slike
            db.collection("incidents")
                .add(incident)
                .addOnSuccessListener {
                    onUploadComplete(null)
                }
        }
    }
}