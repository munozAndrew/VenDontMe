package com.example.vendontme.ui.receipt

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.vendontme.core.Screen
import com.example.vendontme.di.AppModule

data class ReceiptItemInput(
    val name: String = "",
    val price: String = "",
    val quantity: String = "1"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReceiptItemsScreen(
    groupId: String,
    imageUri: String,
    navController: NavController
) {
    val context = LocalContext.current

    val viewModel: ReceiptViewModel = viewModel(
        factory = AppModule.provideReceiptViewModelFactory(context)
    )

    var items by remember { mutableStateOf(listOf(ReceiptItemInput())) }
    var description by remember { mutableStateOf("") }

    val totalAmount = items.sumOf {
        (it.price.toDoubleOrNull() ?: 0.0) * (it.quantity.toIntOrNull() ?: 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Items") }
            )
        },
        bottomBar = {
            // Total and Save button
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Total:",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            "$${String.format("%.2f", totalAmount)}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.saveReceipt(
                                groupId = groupId,
                                imageUri = Uri.parse(imageUri),
                                items = items,
                                totalAmount = totalAmount,
                                description = description,
                                onSuccess = {
                                    // Pop back to group detail (skip the capture screen)
                                    navController.popBackStack(
                                        route = Screen.GroupDetail.route,
                                        inclusive = false
                                    )
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = items.any { it.name.isNotBlank() && it.price.isNotBlank() }
                    ) {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Save Receipt")
                        }
                    }

                    viewModel.error?.let { error ->
                        Spacer(Modifier.height(8.dp))
                        Text(
                            error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Receipt image preview
            item {
                Card {
                    Image(
                        painter = rememberAsyncImagePainter(Uri.parse(imageUri)),
                        contentDescription = "Receipt",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Description
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    placeholder = { Text("e.g., Grocery shopping") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Items header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Items",
                        style = MaterialTheme.typography.titleMedium
                    )

                    TextButton(
                        onClick = {
                            items = items + ReceiptItemInput()
                        }
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Add Item")
                    }
                }
            }

            // Items list
            itemsIndexed(items) { index, item ->
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Item ${index + 1}",
                                style = MaterialTheme.typography.titleSmall
                            )

                            if (items.size > 1) {
                                IconButton(
                                    onClick = {
                                        items = items.filterIndexed { i, _ -> i != index }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        "Delete",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Item name
                        OutlinedTextField(
                            value = item.name,
                            onValueChange = { newValue ->
                                items = items.toMutableList().also {
                                    it[index] = item.copy(name = newValue)
                                }
                            },
                            label = { Text("Item name") },
                            placeholder = { Text("e.g., Milk") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Price
                            OutlinedTextField(
                                value = item.price,
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                        items = items.toMutableList().also {
                                            it[index] = item.copy(price = newValue)
                                        }
                                    }
                                },
                                label = { Text("Price") },
                                placeholder = { Text("0.00") },
                                prefix = { Text("$") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )

                            // Quantity
                            OutlinedTextField(
                                value = item.quantity,
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d+$"))) {
                                        items = items.toMutableList().also {
                                            it[index] = item.copy(quantity = newValue)
                                        }
                                    }
                                },
                                label = { Text("Qty") },
                                placeholder = { Text("1") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(0.5f)
                            )
                        }

                        // Subtotal
                        val subtotal = (item.price.toDoubleOrNull() ?: 0.0) *
                                (item.quantity.toIntOrNull() ?: 1)
                        if (subtotal > 0) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Subtotal: $${String.format("%.2f", subtotal)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}