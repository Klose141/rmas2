package com.example.vandalizam

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AuthScreen(
    isRegister: Boolean = false, // Ovaj parametar definiše da li je ekran za prijavu ili registraciju
    onLoginSuccess: () -> Unit,  // Kada je prijava uspešna
    onRegisterClick: () -> Unit = {}  // Kada korisnik želi da pređe na registraciju (podrazumevana vrednost)
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val viewModel: AuthViewModel = viewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = if (isRegister) "Registracija" else "Prijava", style = MaterialTheme.typography.h4)

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Lozinka") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (isRegister) {
                    viewModel.registerUser(email, password, "Ime i Prezime", "1234567890", null) { success, message ->
                        if (success) {
                            onLoginSuccess()
                        } else {
                            // Prikaži grešku
                        }
                    }
                } else {
                    viewModel.loginUser(email, password) { success, message ->
                        if (success) {
                            onLoginSuccess()
                        } else {
                            // Prikaži grešku
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (isRegister) "Registruj se" else "Prijavi se")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { onRegisterClick() }) {
            Text(text = if (isRegister) "Već imate nalog? Prijavi se" else "Nemate nalog? Registruj se")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    AuthScreen(onLoginSuccess = {}, onRegisterClick = {}) // Dodajemo vrednost za onRegisterClick
}