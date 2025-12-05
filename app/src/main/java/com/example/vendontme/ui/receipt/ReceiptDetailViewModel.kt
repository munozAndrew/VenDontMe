package com.example.vendontme.ui.receipt

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vendontme.data.model.GroupMember
import com.example.vendontme.data.model.Profile
import com.example.vendontme.data.model.Receipt
import com.example.vendontme.data.model.ReceiptItem
import com.example.vendontme.data.repository.GroupRepository
import com.example.vendontme.data.repository.ReceiptRepository
import kotlinx.coroutines.launch
import java.util.UUID

data class ReceiptDetailUiState(
    val receipt: Receipt? = null,
    val items: List<ReceiptItem> = emptyList(),
    val editableItems: List<EditableReceiptItem> = emptyList(),
    val editableDescription: String = "",
    val groupMembers: List<GroupMemberUi> = emptyList(),
    val itemAssignments: Map<Int, List<String>> = emptyMap(),
    val calculatedTotal: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class EditableReceiptItem(
    val id: String,
    val name: String,
    val price: String,
    val quantity: String
)

data class GroupMemberUi(
    val id: String,
    val displayName: String,
    val username: String
)

class ReceiptDetailViewModel(
    private val receiptRepository: ReceiptRepository,
    private val groupRepository: GroupRepository,
    private val receiptId: String
) : ViewModel() {

    var uiState by mutableStateOf(ReceiptDetailUiState())
        private set

    init {
        loadReceiptDetails()
    }

    fun loadReceiptDetails() {
        viewModelScope.launch {
            try {
                uiState = uiState.copy(isLoading = true, error = null)

                println("DEBUG: Loading receipt details for $receiptId")

                val receiptResult = receiptRepository.getReceiptById(receiptId)
                if (receiptResult.isFailure) {
                    throw receiptResult.exceptionOrNull() ?: Exception("Failed to load receipt")
                }

                val receipt = receiptResult.getOrThrow()

                val itemsResult = receiptRepository.getItemsForReceipt(receiptId)
                val items = itemsResult.getOrNull() ?: emptyList()

                println("DEBUG: Loaded receipt with ${items.size} items")

                val editableItems = items.map { item ->
                    EditableReceiptItem(
                        id = item.id,
                        name = item.name,
                        price = item.price.toString(),
                        quantity = item.quantity.toString()
                    )
                }

                val groupMembers: List<GroupMemberUi> = try {
                    val membersResult = groupRepository.getGroupMembers(receipt.groupId)
                    membersResult.fold(
                        onSuccess = { pairs ->
                            pairs.map { pair: Pair<GroupMember, Profile> ->
                                val member = pair.first
                                val profile = pair.second
                                GroupMemberUi(
                                    id = member.userId,
                                    displayName = profile.displayName
                                        ?: profile.username
                                        ?: "Unknown",
                                    username = profile.username ?: "unknown"
                                )
                            }
                        },
                        onFailure = {
                            emptyList()
                        }
                    )
                } catch (e: Exception) {
                    println("DEBUG: Failed to load group members for receipt: ${e.message}")
                    emptyList()
                }

                val total = editableItems.sumOf {
                    (it.price.toDoubleOrNull() ?: 0.0) * (it.quantity.toIntOrNull() ?: 1)
                }

                uiState = uiState.copy(
                    receipt = receipt,
                    items = items,
                    editableItems = editableItems,
                    editableDescription = receipt.description ?: "",
                    groupMembers = groupMembers,
                    calculatedTotal = total,
                    isLoading = false
                )

            } catch (e: Exception) {
                println("DEBUG: Error loading receipt - ${e.message}")
                e.printStackTrace()
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load receipt"
                )
            }
        }
    }

    fun updateDescription(description: String) {
        uiState = uiState.copy(editableDescription = description)
    }

    fun updateItemName(index: Int, name: String) {
        val updatedItems = uiState.editableItems.toMutableList()
        if (index in updatedItems.indices) {
            updatedItems[index] = updatedItems[index].copy(name = name)
            uiState = uiState.copy(editableItems = updatedItems)
        }
    }

    fun updateItemPrice(index: Int, price: String) {
        if (price.isEmpty() || price.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            val updatedItems = uiState.editableItems.toMutableList()
            if (index in updatedItems.indices) {
                updatedItems[index] = updatedItems[index].copy(price = price)
                updateTotal(updatedItems)
            }
        }
    }

    fun updateItemQuantity(index: Int, quantity: String) {
        if (quantity.isEmpty() || quantity.matches(Regex("^\\d+$"))) {
            val updatedItems = uiState.editableItems.toMutableList()
            if (index in updatedItems.indices) {
                updatedItems[index] = updatedItems[index].copy(quantity = quantity)
                updateTotal(updatedItems)
            }
        }
    }

    fun deleteItem(index: Int) {
        val updatedItems = uiState.editableItems.toMutableList()
        if (index in updatedItems.indices) {
            updatedItems.removeAt(index)
            updateTotal(updatedItems)
        }
    }

    fun addNewItem() {
        val updatedItems = uiState.editableItems.toMutableList()
        updatedItems.add(
            EditableReceiptItem(
                id = "",
                name = "",
                price = "",
                quantity = "1"
            )
        )
        uiState = uiState.copy(editableItems = updatedItems)
    }

    fun assignItemToUser(itemIndex: Int, userId: String) {
        val currentAssignments = uiState.itemAssignments.toMutableMap()
        val assignedUsers = currentAssignments[itemIndex]?.toMutableList() ?: mutableListOf()

        if (!assignedUsers.contains(userId)) {
            assignedUsers.add(userId)
            currentAssignments[itemIndex] = assignedUsers
            uiState = uiState.copy(itemAssignments = currentAssignments)

            println("DEBUG: Assigned item $itemIndex to user $userId")
        }
    }

    fun unassignItemFromUser(itemIndex: Int, userId: String) {
        val currentAssignments = uiState.itemAssignments.toMutableMap()
        val assignedUsers = currentAssignments[itemIndex]?.toMutableList() ?: mutableListOf()

        assignedUsers.remove(userId)

        if (assignedUsers.isEmpty()) {
            currentAssignments.remove(itemIndex)
        } else {
            currentAssignments[itemIndex] = assignedUsers
        }

        uiState = uiState.copy(itemAssignments = currentAssignments)

        println("DEBUG: Unassigned user $userId from item $itemIndex")
    }

    private fun updateTotal(items: List<EditableReceiptItem>) {
        val total = items.sumOf {
            (it.price.toDoubleOrNull() ?: 0.0) * (it.quantity.toIntOrNull() ?: 1)
        }
        uiState = uiState.copy(editableItems = items, calculatedTotal = total)
    }

    fun saveChanges() {
        viewModelScope.launch {
            try {
                println("DEBUG: Saving changes...")
                uiState = uiState.copy(isLoading = true, error = null)

                val receipt = uiState.receipt ?: return@launch

                val updatedReceipt = receipt.copy(
                    description = uiState.editableDescription,
                    totalAmount = uiState.calculatedTotal
                )

                val updateReceiptResult = receiptRepository.updateReceipt(updatedReceipt)
                if (updateReceiptResult.isFailure) {
                    throw updateReceiptResult.exceptionOrNull() ?: Exception("Failed to update receipt")
                }

                println("DEBUG: Receipt updated successfully")

                uiState.editableItems.forEach { editableItem ->
                    if (editableItem.id.isNotEmpty()) {
                        if (editableItem.name.isNotBlank() && editableItem.price.isNotBlank()) {
                            val updatedItem = ReceiptItem(
                                id = editableItem.id,
                                receiptId = receiptId,
                                name = editableItem.name,
                                price = editableItem.price.toDoubleOrNull() ?: 0.0,
                                quantity = editableItem.quantity.toIntOrNull() ?: 1
                            )

                            val updateResult = receiptRepository.updateReceiptItem(updatedItem)
                            if (updateResult.isSuccess) {
                                println("DEBUG: Updated item ${editableItem.id}")
                            }
                        }
                    }
                }

                println("DEBUG: All changes saved successfully")

                kotlinx.coroutines.delay(300)
                loadReceiptDetails()

            } catch (e: Exception) {
                println("DEBUG: Error saving - ${e.message}")
                e.printStackTrace()
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save"
                )
            }
        }
    }

    fun cancelEdit() {
        loadReceiptDetails()
    }
}
