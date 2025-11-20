package com.example.vendontme.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SettingsScreen(nav: NavController) {

    Box(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text("Settings", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(20.dp))

            Button(onClick = { nav.navigateUp() }) {
                Text("Back")
            }
        }
    }
}
