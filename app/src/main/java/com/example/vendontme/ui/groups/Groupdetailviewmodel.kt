package com.example.vendontme.ui.groups

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vendontme.data.model.Receipt
import com.example.vendontme.data.repository.GroupRepository
import com.example.vendontme.data.repository.ReceiptRepository
import kotlinx.coroutines.launch

data class GroupDetailUiState(
    val group: GroupUi? = null,
    val members: List<MemberUi> = emptyList(),
    val receipts: List<Receipt> = emptyList(),  // Changed from ReceiptUi to Receipt
    val isLoading: Boolean = false,
    val error: String? = null
)

data class GroupUi(
    val id: String,
    val name: String,
    val description: String?
)

data class MemberUi(
    val id: String,
    val displayName: String,
    val username: String,
    val role: String
)

class GroupDetailViewModel(
    private val groupRepository: GroupRepository,
    private val receiptRepository: ReceiptRepository,
    private val groupId: String
) : ViewModel() {

    var uiState by mutableStateOf(GroupDetailUiState())
        private set

    init {
        loadGroupDetails()
        loadReceipts()
    }

    fun loadGroupDetails() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            // Mock group data (replace with actual repository call)
            val mockGroup = GroupUi(
                id = groupId,
                name = "Group Name",
                description = "Group description"
            )

            val mockMembers = listOf(
                MemberUi(
                    id = "1",
                    displayName = "Anthony",
                    username = "Anthony",
                    role = "admin"
                )
            )

            uiState = uiState.copy(
                group = mockGroup,
                members = mockMembers,
                isLoading = false
            )
        }
    }

    fun loadReceipts() {
        viewModelScope.launch {
            println("DEBUG: Loading receipts for group $groupId")

            val result = receiptRepository.getReceiptsForGroup(groupId)

            if (result.isSuccess) {
                val receipts = result.getOrNull() ?: emptyList()
                println("DEBUG: Loaded ${receipts.size} receipts")

                uiState = uiState.copy(receipts = receipts)
            } else {
                println("DEBUG: Failed to load receipts - ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun refreshReceipts() {
        loadReceipts()
    }
}