package com.example.vandalizam

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
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
fun IncidentListScreen(
    onIncidentSelected: (String) -> Unit,  // Parametar za izbor incidenta
    onAddIncidentClicked: () -> Unit       // Parametar za dodavanje novog incidenta
) {
    val incidents = remember { listOf<Incident>() } // Pretpostavljamo da incidenti dolaze iz ViewModel-a

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Dugme za dodavanje incidenta
        Button(onClick = onAddIncidentClicked) {
            Text("Dodaj Incident")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(incidents) { incident ->
                IncidentListItem(incident = incident, onClick = {
                    onIncidentSelected(incident.id) // Pozivamo funkciju za izbor incidenta
                })
            }
        }
    }
}

@Composable
fun IncidentListItem(incident: Incident, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp).clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = incident.title)
    }
}

data class Incident(
    val id: String,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double
)

class IncidentListViewModel : ViewModel() {
    private val _incidents = mutableStateListOf<Incident>()
    val incidents: List<Incident> = _incidents

    init {
        fetchIncidents()
    }

    private fun fetchIncidents() {
        val db = FirebaseFirestore.getInstance()
        db.collection("incidents")
            .get()
            .addOnSuccessListener { result ->
                _incidents.clear()
                for (document in result) {
                    val id = document.id // Preuzimamo ID dokumenta iz Firestore-a
                    val title = document.getString("title") ?: ""
                    val description = document.getString("description") ?: ""
                    val latitude = document.getDouble("latitude") ?: 0.0
                    val longitude = document.getDouble("longitude") ?: 0.0

                    // Dodajemo incident sa ID-jem u listu
                    _incidents.add(Incident(id, title, description, latitude, longitude))
                }
            }
    }
}