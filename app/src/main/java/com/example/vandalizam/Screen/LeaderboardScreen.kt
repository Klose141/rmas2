package com.example.vandalizam

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LeaderboardScreen(viewModel: LeaderboardViewModel = viewModel()) {
    val leaderboardEntries = viewModel.leaderboardEntries // Direktan pristup listi

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Rang lista korisnika", modifier = Modifier.padding(bottom = 16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(leaderboardEntries) { entry ->
                LeaderboardItem(entry)
            }
        }
    }
}

@Composable
fun LeaderboardItem(entry: LeaderboardEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = entry.username)
        Text(text = "${entry.points} poena")
    }
}

data class LeaderboardEntry(
    val username: String,
    val points: Int
)

class LeaderboardViewModel : ViewModel() {
    private val _leaderboardEntries = mutableStateListOf<LeaderboardEntry>()
    val leaderboardEntries: List<LeaderboardEntry> = _leaderboardEntries

    init {
        fetchLeaderboardEntries()
    }

    private fun fetchLeaderboardEntries() {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .orderBy("points", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                _leaderboardEntries.clear()
                for (document in result) {
                    val username = document.getString("username") ?: ""
                    val points = document.getLong("points")?.toInt() ?: 0 // Sigurna konverzija u Int
                    _leaderboardEntries.add(LeaderboardEntry(username, points))
                }
            }
    }
}