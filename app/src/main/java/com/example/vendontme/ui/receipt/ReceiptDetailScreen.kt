package com.example.vendontme.ui.receipt

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vendontme.di.AppModule
import com.example.vendontme.data.model.ReceiptItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptDetailScreen(
    receiptId: String,
    navController: NavController
) {
    val viewModel: ReceiptDetailViewModel = viewModel(
        factory = AppModule.provideReceiptDetailViewModelFactory(receiptId)
    )

    val uiState = viewModel.uiState
    var isEditMode by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Receipt" else "Receipt Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (!isEditMode) {
                        IconButton(onClick = { isEditMode = true }) {
                            Icon(Icons.Default.Edit, "Edit")
                        }
                    } else {
                        TextButton(onClick = {
                            viewModel.saveChanges()
                            isEditMode = false
                        }) {
                            Text("Save")
                        }
                        TextButton(onClick = {
                            viewModel.cancelEdit()
                            isEditMode = false
                        }) {
                            Text("Cancel")
                        }
                    }
                }
            )
        }
    ) { padding ->

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        uiState.error,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadReceiptDetails() }) {
                        Text("Retry")
                    }
                }
            }
        } else {
            ReceiptDetailContent(
                uiState = uiState,
                isEditMode = isEditMode,
                onDescriptionChange = { viewModel.updateDescription(it) },
                onItemNameChange = { index, name -> viewModel.updateItemName(index, name) },
                onItemPriceChange = { index, price -> viewModel.updateItemPrice(index, price) },
                onItemQuantityChange = { index, qty -> viewModel.updateItemQuantity(index, qty) },
                onDeleteItem = { index -> viewModel.deleteItem(index) },
                onAddItem = { viewModel.addNewItem() },
                onAssignItem = { itemIndex, userId -> viewModel.assignItemToUser(itemIndex, userId) },
                onUnassignItem = { itemIndex, userId -> viewModel.unassignItemFromUser(itemIndex, userId) },
                padding = padding
            )
        }
    }
}

@Composable
fun ReceiptDetailContent(
    uiState: ReceiptDetailUiState,
    isEditMode: Boolean,
    onDescriptionChange: (String) -> Unit,
    onItemNameChange: (Int, String) -> Unit,
    onItemPriceChange: (Int, String) -> Unit,
    onItemQuantityChange: (Int, String) -> Unit,
    onDeleteItem: (Int) -> Unit,
    onAddItem: () -> Unit,
    onAssignItem: (Int, String) -> Unit,
    onUnassignItem: (Int, String) -> Unit,
    padding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Receipt Image
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = uiState.receipt?.imageUrl,
                    contentDescription = "Receipt",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        // Description
        item {
            if (isEditMode) {
                OutlinedTextField(
                    value = uiState.editableDescription,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Description",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            uiState.receipt?.description ?: "No description",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        // Items Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Items",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (isEditMode) {
                    TextButton(onClick = onAddItem) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Add Item")
                    }
                }
            }
        }

        // Items List
        items(uiState.editableItems.size) { index ->
            val item = uiState.editableItems[index]

            ReceiptItemCard(
                item = item,
                index = index,
                isEditMode = isEditMode,
                groupMembers = uiState.groupMembers,
                assignedUsers = uiState.itemAssignments[index] ?: emptyList(),
                onNameChange = { onItemNameChange(index, it) },
                onPriceChange = { onItemPriceChange(index, it) },
                onQuantityChange = { onItemQuantityChange(index, it) },
                onDelete = { onDeleteItem(index) },
                onAssignUser = { userId -> onAssignItem(index, userId) },
                onUnassignUser = { userId -> onUnassignItem(index, userId) }
            )
        }

        // Total
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Total:",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "$${String.format("%.2f", uiState.calculatedTotal)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Split Summary (who owes what)
        if (!isEditMode && uiState.itemAssignments.isNotEmpty()) {
            item {
                Text(
                    "Split Summary",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                SplitSummaryCard(
                    groupMembers = uiState.groupMembers,
                    itemAssignments = uiState.itemAssignments,
                    items = uiState.editableItems
                )
            }
        }
    }
}

@Composable
fun ReceiptItemCard(
    item: EditableReceiptItem,
    index: Int,
    isEditMode: Boolean,
    groupMembers: List<GroupMemberUi>,
    assignedUsers: List<String>,
    onNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onDelete: () -> Unit,
    onAssignUser: (String) -> Unit,
    onUnassignUser: (String) -> Unit
) {
    var showAssignDialog by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Item ${index + 1}",
                    style = MaterialTheme.typography.titleSmall
                )

                Row {
                    if (!isEditMode) {
                        IconButton(onClick = { showAssignDialog = true }) {
                            Icon(Icons.Default.Person, "Assign")
                        }
                    }
                    if (isEditMode) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (isEditMode) {
                // Edit mode
                OutlinedTextField(
                    value = item.name,
                    onValueChange = onNameChange,
                    label = { Text("Item name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = item.price,
                        onValueChange = onPriceChange,
                        label = { Text("Price") },
                        prefix = { Text("$") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = item.quantity,
                        onValueChange = onQuantityChange,
                        label = { Text("Qty") },
                        modifier = Modifier.weight(0.5f)
                    )
                }
            } else {
                // View mode
                Text(
                    item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "$${item.price} × ${item.quantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "$${String.format("%.2f",
                            (item.price.toDoubleOrNull() ?: 0.0) *
                                    (item.quantity.toIntOrNull() ?: 1)
                        )}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Show assigned users
            if (assignedUsers.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                Text(
                    "Assigned to:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(4.dp))

                assignedUsers.forEach { userId ->
                    val member = groupMembers.find { it.id == userId }
                    member?.let {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                it.displayName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (isEditMode) {
                                IconButton(onClick = { onUnassignUser(userId) }) {
                                    Icon(
                                        Icons.Default.Close,
                                        "Remove",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Assign Dialog
    if (showAssignDialog) {
        AssignUserDialog(
            groupMembers = groupMembers,
            assignedUsers = assignedUsers,
            onAssign = { userId ->
                onAssignUser(userId)
                showAssignDialog = false
            },
            onDismiss = { showAssignDialog = false }
        )
    }
}

@Composable
fun AssignUserDialog(
    groupMembers: List<GroupMemberUi>,
    assignedUsers: List<String>,
    onAssign: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign to Member") },
        text = {
            LazyColumn {
                items(groupMembers) { member ->
                    val isAssigned = assignedUsers.contains(member.id)

                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        onClick = { if (!isAssigned) onAssign(member.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    member.displayName,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    "@${member.username}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isAssigned) {
                                Icon(
                                    Icons.Default.Check,
                                    "Assigned",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun SplitSummaryCard(
    groupMembers: List<GroupMemberUi>,
    itemAssignments: Map<Int, List<String>>,
    items: List<EditableReceiptItem>
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Calculate what each person owes
            val userTotals = mutableMapOf<String, Double>()

            itemAssignments.forEach { (itemIndex, userIds) ->
                if (itemIndex < items.size && userIds.isNotEmpty()) {
                    val item = items[itemIndex]
                    val itemTotal = (item.price.toDoubleOrNull() ?: 0.0) *
                            (item.quantity.toIntOrNull() ?: 1)
                    val perPerson = itemTotal / userIds.size

                    userIds.forEach { userId ->
                        userTotals[userId] = (userTotals[userId] ?: 0.0) + perPerson
                    }
                }
            }

            userTotals.forEach { (userId, amount) ->
                val member = groupMembers.find { it.id == userId }
                member?.let {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            it.displayName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "$${String.format("%.2f", amount)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}