package com.example.vendontme.ui.friends

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.vendontme.di.AppModule
import com.example.vendontme.ui.home.components.FriendsList
import com.example.vendontme.ui.home.components.PendingRequestsList
import com.example.vendontme.ui.home.components.SearchUsersView
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(nav: NavController) {
    val viewModel: FriendViewModel = viewModel(
        factory = AppModule.provideFriendViewModelFactory()
    )

    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friends") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Friends") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row {
                            Text("Requests")
                            if (uiState.pendingRequests.isNotEmpty()) {
                                Spacer(Modifier.width(4.dp))
                                Badge {
                                    Text("${uiState.pendingRequests.size}")
                                }
                            }
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Search") }
                )
            }

            when (selectedTab) {
                0 -> FriendsList(
                    friends = uiState.friends,
                    isLoading = uiState.isLoading,
                    onRemoveFriend = { friendshipId ->
                        viewModel.removeFriend(friendshipId)
                    }
                )
                1 -> PendingRequestsList(
                    pendingRequests = uiState.pendingRequests,
                    isLoading = uiState.isLoading,
                    onAccept = { friendshipId ->
                        viewModel.acceptFriendRequest(friendshipId)
                    },
                    onReject = { friendshipId ->
                        viewModel.rejectFriendRequest(friendshipId)
                    }
                )
                2 -> SearchUsersView(
                    searchQuery = uiState.searchQuery,
                    searchResults = uiState.searchResults,
                    isLoading = uiState.isLoading,
                    onSearchQueryChange = { query ->
                        viewModel.searchUsers(query)
                    },
                    onSendRequest = { friendId ->
                        viewModel.sendFriendRequest(friendId)
                    },
                    onClearSearch = {
                        viewModel.clearSearchResults()
                    }
                )
            }
        }
    }
}