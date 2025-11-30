package com.example.vendontme.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vendontme.di.AppModule
import com.example.vendontme.ui.home.components.GroupCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onGroupClick: (String) -> Unit = {},
    onCreateGroupClick: () -> Unit = {},
    onFriendsClick: () -> Unit = {}

) {
    val viewModel: HomeViewModel = viewModel(
        factory = AppModule.provideHomeViewModelFactory()
    )

    val uiState by viewModel.uiState.collectAsState()


    DisposableEffect(Unit) {
        viewModel.loadGroups()
        onDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Split It") },
                actions = {
                    IconButton(onClick = onFriendsClick) {
                        Icon(Icons.Default.Person, contentDescription = "Friends")
                    }
                }

            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateGroupClick
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Group")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingView()
                }

                uiState.groups.isEmpty() -> {
                    EmptyGroupsView()
                }

                else -> {
                    GroupsList(
                        groups = uiState.groups,
                        onGroupClick = onGroupClick
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyGroupsView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "No groups yet",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Tap + to create one",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun GroupsList(
    groups: List<GroupUi>,
    onGroupClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        items(groups) { group ->
            GroupCard(
                name = group.name,
                memberCount = group.membersCount,
                onClick = { onGroupClick(group.id) }
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}