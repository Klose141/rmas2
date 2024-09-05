package com.example.vandalizam

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance().reference

    fun registerUser(
        email: String,
        password: String,
        fullName: String,
        phoneNumber: String,
        profilePictureUri: Uri?,
        onComplete: (Boolean, String?) -> Unit
    ) {
        // Provera da li su polja prazna
        if (email.isEmpty() || password.isEmpty() || fullName.isEmpty() || phoneNumber.isEmpty()) {
            onComplete(false, "Sva polja moraju biti popunjena.")
            return
        }

        // Kreiranje korisnika sa emailom i lozinkom
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    // Provera da li postoji profilna slika i njen upload
                    if (profilePictureUri != null) {
                        uploadProfilePicture(user, profilePictureUri, onComplete)
                    } else {
                        onComplete(true, null)
                    }
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    private fun uploadProfilePicture(
        user: FirebaseUser?,
        profilePictureUri: Uri,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val userId = user?.uid ?: return
        val profileRef = storage.child("vandalizamProfilePictures/$userId.jpg")

        // Upload slike
        profileRef.putFile(profilePictureUri)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    profileRef.downloadUrl.addOnSuccessListener { uri ->
                        // URL se može sačuvati u Firestore ako je potrebno
                        onComplete(true, null)
                    }
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun loginUser(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        if (email.isEmpty() || password.isEmpty()) {
            onComplete(false, "Email i lozinka moraju biti popunjeni.")
            return
        }

        // Prijava korisnika
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun logout() {
        auth.signOut()
    }
}