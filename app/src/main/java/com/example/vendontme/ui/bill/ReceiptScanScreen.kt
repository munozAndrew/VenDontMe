package com.example.vendontme.ui.bill

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vendontme.core.Screen

@Composable
fun ReceiptScanScreen(nav: NavController, groupId: String) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text("Scan Receipt (Group $groupId)", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    nav.navigate(Screen.EditReceipt.route.replace("{groupId}", groupId))
                }
            ) {
                Text("Continue")
            }
        }
    }
}
