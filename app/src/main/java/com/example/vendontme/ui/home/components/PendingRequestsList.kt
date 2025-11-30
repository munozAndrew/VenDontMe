package com.example.vendontme.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vendontme.data.model.Friend

@Composable
fun PendingRequestsList(
    pendingRequests: List<Friend>,
    isLoading: Boolean,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            pendingRequests.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "No pending requests",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Friend requests will appear here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    items(pendingRequests) { request ->
                        PendingRequestCard(
                            request = request,
                            onAccept = { onAccept(request.id ?: "") },
                            onReject = { onReject(request.id ?: "") }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}