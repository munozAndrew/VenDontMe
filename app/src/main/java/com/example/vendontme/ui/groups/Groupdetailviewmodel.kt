package com.example.vendontme.ui.groups

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vendontme.data.model.Group
import com.example.vendontme.data.model.Profile
import com.example.vendontme.data.repository.GroupRepository
import kotlinx.coroutines.launch

class GroupDetailViewModel(
    private val groupRepository: GroupRepository,
    private val groupId: String
) : ViewModel() {

    var uiState by mutableStateOf(GroupDetailUiState())
        private set

    init {
        loadGroupDetails()
    }

    fun loadGroupDetails() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            try {
                // Fetch group info
                val group = groupRepository.getGroupById(groupId)

                if (group == null) {
                    uiState = GroupDetailUiState(
                        isLoading = false,
                        error = "Group not found"
                    )
                    return@launch
                }

                // Fetch members with their profiles
                val membersWithProfiles = groupRepository.getGroupMembers(groupId)

                uiState = GroupDetailUiState(
                    isLoading = false,
                    group = group,
                    members = membersWithProfiles.map { (member, profile) ->
                        MemberUi(
                            userId = member.userId,
                            displayName = profile.displayName ?: profile.username ?: "Unknown",
                            username = profile.username ?: "",
                            avatarUrl = profile.avatarUrl,
                            role = member.role ?: "member"
                        )
                    }
                )

            } catch (e: Exception) {
                uiState = GroupDetailUiState(
                    isLoading = false,
                    error = e.message ?: "Failed to load group"
                )
            }
        }
    }
}

data class GroupDetailUiState(
    val isLoading: Boolean = false,
    val group: Group? = null,
    val members: List<MemberUi> = emptyList(),
    val error: String? = null
)

data class MemberUi(
    val userId: String,
    val displayName: String,
    val username: String,
    val avatarUrl: String?,
    val role: String
)