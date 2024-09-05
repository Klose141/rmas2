import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(userLocation: LatLng?, incidentLocations: List<LatLng>) {
    var currentLocation by remember { mutableStateOf(userLocation) }

    // Ako je userLocation null, postavi default lokaciju
    val initialLocation = currentLocation ?: LatLng(44.7866, 20.4489) // Beograd kao default lokacija

    // Kreiraj CameraPosition koristeći CameraPosition.Builder
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.Builder()
            .target(initialLocation)
            .zoom(12f)
            .build()
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        // Prikazivanje markera za trenutnu lokaciju
        if (currentLocation != null) {
            Marker(
                state = com.google.maps.android.compose.MarkerState(position = currentLocation!!),
                title = "Moja trenutna lokacija"
            )
        }

        // Prikazivanje markera za incidente
        for (incident in incidentLocations) {
            Marker(
                state = com.google.maps.android.compose.MarkerState(position = incident),
                title = "Incident"
            )
        }
    }
}