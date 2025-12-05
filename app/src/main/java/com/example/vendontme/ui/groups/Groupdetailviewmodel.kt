package com.example.vendontme.ui.groups

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vendontme.data.model.GroupMember
import com.example.vendontme.data.model.Profile
import com.example.vendontme.data.model.Receipt
import com.example.vendontme.data.repository.GroupRepository
import com.example.vendontme.data.repository.ReceiptRepository
import kotlinx.coroutines.launch


data class GroupDetailUiState(
    val group: GroupUi? = null,
    val members: List<MemberUi> = emptyList(),
    val receipts: List<Receipt> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

data class GroupUi(
    val id: String,
    val name: String,
    val description: String?
)

data class MemberUi(
    val userId: String,
    val displayName: String,
    val username: String,
    val avatarUrl: String?,
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
        loadAll()
    }

    // Load everything
    fun loadAll() {
        loadGroupDetails()
        loadReceipts()
    }

    fun loadGroupDetails() {
        viewModelScope.launch {
            try {
                uiState = uiState.copy(isLoading = true, error = null, successMessage = null)

                val groupResult = groupRepository.getGroupById(groupId)
                val group = groupResult.getOrElse {
                    uiState = uiState.copy(
                        isLoading = false,
                        error = it.message ?: "Failed to load group"
                    )
                    return@launch
                }

                val groupUi = GroupUi(
                    id = group.id ?: "",
                    name = group.name ?: "Unnamed Group",
                    description = group.description
                )

                val membersResult = groupRepository.getGroupMembers(groupId)

                val members = membersResult.fold(
                    onSuccess = { pairs ->
                        pairs.map { (member: GroupMember, profile: Profile) ->
                            MemberUi(
                                userId = member.userId,
                                displayName = profile.displayName ?: profile.username ?: "Unknown",
                                username = profile.username ?: "unknown",
                                avatarUrl = profile.avatarUrl,
                                role = member.role ?: "member"
                            )
                        }
                    },
                    onFailure = {
                        emptyList()
                    }
                )

                uiState = uiState.copy(
                    group = groupUi,
                    members = members,
                    isLoading = false
                )

            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Unexpected error loading group"
                )
            }
        }
    }


    fun loadReceipts() {
        viewModelScope.launch {
            val result = receiptRepository.getReceiptsForGroup(groupId)
            uiState = uiState.copy(receipts = result.getOrNull() ?: emptyList())
        }
    }

    fun refreshReceipts() = loadReceipts()


    fun addMember(userId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null, successMessage = null)

            val result = groupRepository.addMember(groupId, userId)

            if (result.isSuccess) {
                val (groupMember, profile) = result.getOrThrow()

                // Build new MemberUi entry
                val newMemberUi = MemberUi(
                    userId = groupMember.userId,
                    displayName = profile.displayName ?: profile.username ?: "Unknown",
                    username = profile.username ?: "unknown",
                    avatarUrl = profile.avatarUrl,
                    role = groupMember.role ?: "member"
                )

                // Append to existing list without refetching the whole group
                val updatedMembers = uiState.members + newMemberUi

                uiState = uiState.copy(
                    members = updatedMembers,
                    isLoading = false,
                    successMessage = "Member added successfully!"
                )

            } else {
                uiState = uiState.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to add member"
                )
            }
        }
    }

    fun clearError() {
        uiState = uiState.copy(error = null)
    }

    fun clearSuccess() {
        uiState = uiState.copy(successMessage = null)
    }
}
