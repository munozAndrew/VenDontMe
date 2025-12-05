package com.example.vendontme.ui.groups

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vendontme.R
import com.example.vendontme.core.Screen
import com.example.vendontme.data.model.Friend
import com.example.vendontme.data.model.getAvatarUrl
import com.example.vendontme.data.model.getDisplayName
import com.example.vendontme.data.model.getUsername
import com.example.vendontme.di.AppModule
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    navController: NavController,
    groupId: String
) {
    val viewModel: GroupDetailViewModel = viewModel(
        factory = AppModule.provideGroupDetailViewModelFactory(groupId)
    )

    val uiState = viewModel.uiState
    var showAddMemberDialog by remember { mutableStateOf(false) }

    // Whenever the screen loads, refresh receipts
    LaunchedEffect(Unit) {
        viewModel.refreshReceipts()
    }

    // Snackbars for success & errors
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
        viewModel.clearError()
    }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { snackbarHostState.showSnackbar(it) }
        viewModel.clearSuccess()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(uiState.group?.name ?: "Group Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshReceipts() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                    IconButton(onClick = { /* TODO settings */ }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.CaptureReceipt.pass(groupId)) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.photo_cam),
                        contentDescription = "Camera"
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("New Split")
                }
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
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(uiState.error, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadAll() }) { Text("Retry") }
                    }
                }

                else -> {
                    GroupDetailContent(
                        uiState = uiState,
                        navController = navController,
                        onAddMember = { showAddMemberDialog = true }
                    )
                }
            }
        }
    }

    if (showAddMemberDialog) {
        AddMemberDialog(
            onDismiss = { showAddMemberDialog = false },
            onMemberSelected = { friendUserId ->
                viewModel.addMember(friendUserId)
                showAddMemberDialog = false
            }
        )
    }
}

@Composable
fun AddMemberDialog(
    onDismiss: () -> Unit,
    onMemberSelected: (String) -> Unit
) {
    val friendViewModel: com.example.vendontme.ui.friends.FriendViewModel =
        viewModel(factory = AppModule.provideFriendViewModelFactory())

    val uiState by friendViewModel.uiState.collectAsState()

    // Get current user ID from AuthRepository
    val authRepository = AppModule.provideAuthRepository()
    val currentUserId = authRepository.getCurrentUserId()

    LaunchedEffect(Unit) {
        friendViewModel.loadFriends()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Member to Group") },
        text = {
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                uiState.friends.isEmpty() -> {
                    Text("You have no friends to add.\nSend a friend request first.")
                }

                else -> {
                    LazyColumn {
                        items(uiState.friends) { friend ->
                            AddMemberRow(friend = friend, onClick = {
                                // Correct friend → user mapping
                                val targetUserId =
                                    if (friend.userId == currentUserId) {
                                        friend.friendId
                                    } else {
                                        friend.userId
                                    }

                                onMemberSelected(targetUserId)
                            })
                            Divider()
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun AddMemberRow(friend: Friend, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        Text(friend.getDisplayName(), style = MaterialTheme.typography.bodyLarge)
        if (friend.getUsername().isNotBlank()) {
            Text(
                "@${friend.getUsername()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GroupDetailContent(
    uiState: GroupDetailUiState,
    navController: NavController,
    onAddMember: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        if (!uiState.group?.description.isNullOrBlank()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Description", style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(uiState.group?.description.orEmpty())
                    }
                }
            }
        }


        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Members",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onAddMember) {
                    Icon(Icons.Default.Add, "Add Member")
                }
            }
        }

        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.group),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("${uiState.members.size} Member(s)")
                    }

                    Spacer(Modifier.height(12.dp))

                    uiState.members.forEachIndexed { index, member ->
                        if (index > 0) Divider(Modifier.padding(vertical = 8.dp))
                        MemberItem(member)
                    }
                }
            }
        }


        item {
            Text(
                "Recent Receipts (${uiState.receipts.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (uiState.receipts.isEmpty()) {
            item { EmptyReceiptsCard() }
        } else {
            items(uiState.receipts) { receipt ->
                ReceiptCard(receipt) {
                    navController.navigate(Screen.ReceiptDetail.pass(receipt.id))
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
fun MemberItem(member: MemberUi) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        member.displayName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(member.displayName, style = MaterialTheme.typography.bodyLarge)
                if (member.username.isNotBlank()) {
                    Text(
                        "@${member.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (member.role == "admin") {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    "Admin",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun EmptyReceiptsCard() {
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.description),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(16.dp))
            Text("No receipts yet")
            Spacer(Modifier.height(8.dp))
            Text("Tap “New Split” to scan a receipt")
        }
    }
}

@Composable
fun ReceiptCard(receipt: com.example.vendontme.data.model.Receipt, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp)) {
            AsyncImage(
                model = receipt.imageUrl,
                contentDescription = "Receipt",
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Crop
            )

            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    receipt.description ?: "Receipt",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "$${String.format(Locale.US, "%.2f", receipt.totalAmount)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
