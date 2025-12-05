package com.example.vendontme.ui.groups

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.vendontme.core.Screen
import com.example.vendontme.di.AppModule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(nav: NavController) {

    val homeViewModel: com.example.vendontme.ui.home.HomeViewModel = viewModel(
        factory = AppModule.provideHomeViewModelFactory()
    )

    var groupName by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Group") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Group Name") },
                placeholder = { Text("e.g. Weekend Squad") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !homeViewModel.isCreatingGroup,
                singleLine = true
            )

            OutlinedTextField(
                value = groupDescription,
                onValueChange = { groupDescription = it },
                label = { Text("Description (Optional)") },
                placeholder = { Text("What's this group for?") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !homeViewModel.isCreatingGroup,
                minLines = 3,
                maxLines = 5
            )

            Spacer(Modifier.weight(1f))

            homeViewModel.createError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Button(
                onClick = {
                    if (groupName.isNotBlank()) {
                        homeViewModel.createGroup(
                            name = groupName.trim(),
                            description = groupDescription.trim().ifBlank { null }
                        ) { groupId ->
                            // Navigate to the new group detail screen
                            nav.navigate(Screen.GroupDetail.pass(groupId)) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = groupName.isNotBlank() && !homeViewModel.isCreatingGroup
            ) {
                if (homeViewModel.isCreatingGroup) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Group")
                }
            }
        }
    }
}