package com.example.vendontme.ui.bill

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SplitSummaryScreen(nav: NavController, groupId: String) {

    Box(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text("Split Summary (Group $groupId)", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(20.dp))

            Button(onClick = { nav.navigateUp() }) {
                Text("Done")
            }
        }
    }
}
