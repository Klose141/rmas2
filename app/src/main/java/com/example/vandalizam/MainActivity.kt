package com.example.vandalizam


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.FirebaseApp

import com.google.firebase.auth.FirebaseAuth


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VandalizamApp()
        }
    }
}

@Composable
fun VandalizamApp() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()

    // Odjavi korisnika kada se aplikacija pokrene
    LaunchedEffect(Unit) {
        auth.signOut() // Odjavi korisnika pri svakom pokretanju aplikacije
    }

    var startDestination by remember { mutableStateOf("login") }

    LaunchedEffect(Unit) {
        startDestination = "login"
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Vandalizam") })
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Ekran za prijavu
            composable("login") {
                AuthScreen(
                    onLoginSuccess = {
                        navController.navigate("incidentList") {
                            popUpTo("login") { inclusive = true } // Izbriši ekran za prijavu
                        }
                    },
                    onRegisterClick = {
                        navController.navigate("register")
                    }
                )
            }

            // Ekran za registraciju
            composable("register") {
                AuthScreen(
                    isRegister = true,
                    onLoginSuccess = {
                        navController.navigate("incidentList") {
                            popUpTo("register") { inclusive = true } // Izbriši ekran za registraciju
                        }
                    },
                    onRegisterClick = {
                        navController.navigate("login") // Vrati nazad na prijavu
                    }
                )
            }

            // Ekran sa listom incidenata
            composable("incidentList") {
                IncidentListScreen(
                    onIncidentSelected = { incidentId ->
                        navController.navigate("incidentDetail/$incidentId")
                    },
                    onAddIncidentClicked = {
                        navController.navigate("reportIncident")
                    }
                )
            }

            // Ekran za prijavu incidenta
            composable("reportIncident") {
                val selectedLocation =
                    navController.previousBackStackEntry?.arguments?.getParcelable<LatLng>("selectedLocation")

                ReportIncidentScreen(
                    onIncidentReported = { navController.popBackStack() },
                    onSelectLocation = {
                        navController.navigate("selectLocation")
                    },
                    initialLocation = selectedLocation // Čuva prethodno izabranu lokaciju
                )
            }

            // Ekran za biranje lokacije
            composable("selectLocation") {
                SelectLocationScreen { selectedLocation ->
                    // Sačuvaj izabranu lokaciju i vrati se na prethodni ekran
                    navController.previousBackStackEntry?.arguments?.putParcelable(
                        "selectedLocation",
                        selectedLocation
                    )
                    navController.popBackStack()
                }
            }

            // Ekran za detalje incidenta
            composable("incidentDetail/{incidentId}") { backStackEntry ->
                val incidentId = backStackEntry.arguments?.getString("incidentId") ?: return@composable
                IncidentDetailScreen(
                    incidentId = incidentId,
                    onBackPressed = { navController.popBackStack() }
                )
            }
        }
    }
}