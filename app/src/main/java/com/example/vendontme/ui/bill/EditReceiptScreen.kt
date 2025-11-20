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
fun EditReceiptScreen(nav: NavController, groupId: String) {

    Box(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text("Edit Receipt Items", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    nav.navigate(Screen.SplitBill.route.replace("{groupId}", groupId))
                }
            ) {
                Text("Next: Split Bill")
            }
        }
    }
}
