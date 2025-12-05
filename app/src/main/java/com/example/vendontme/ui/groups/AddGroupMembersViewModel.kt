package com.example.vendontme.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vendontme.data.model.Friend
import com.example.vendontme.data.model.Profile
import com.example.vendontme.data.model.GroupMember
import com.example.vendontme.data.repository.FriendRepository
import com.example.vendontme.data.repository.GroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddMemberCandidateUi(
    val userId: String,
    val displayName: String,
    val username: String,
    val avatarUrl: String?
)

data class AddGroupMembersUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val candidates: List<AddMemberCandidateUi> = emptyList()
)

class AddGroupMembersViewModel(
    private val groupRepository: GroupRepository,
    private val friendRepository: FriendRepository,
    private val groupId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddGroupMembersUiState())
    val uiState: StateFlow<AddGroupMembersUiState> = _uiState.asStateFlow()

    init {
        loadCandidates()
    }

    fun loadCandidates() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)

            try {
                val membersResult = groupRepository.getGroupMembers(groupId)
                val friendsResult = friendRepository.getFriends()

                val currentMemberIds: Set<String> = membersResult.fold(
                    onSuccess = { pairs ->
                        pairs.map { pair: Pair<GroupMember, Profile> ->
                            pair.first.userId
                        }.toSet()
                    },
                    onFailure = {
                        emptySet()
                    }
                )

                val candidates: List<AddMemberCandidateUi> = friendsResult.fold(
                    onSuccess = { friends ->
                        friends.mapNotNull { friend: Friend ->
                            val profile = friend.friendProfile ?: return@mapNotNull null
                            if (profile.id == null) return@mapNotNull null
                            if (currentMemberIds.contains(profile.id)) return@mapNotNull null

                            AddMemberCandidateUi(
                                userId = profile.id,
                                displayName = profile.displayName ?: profile.username ?: "Unknown",
                                username = profile.username ?: "unknown",
                                avatarUrl = profile.avatarUrl
                            )
                        }
                    },
                    onFailure = {
                        emptyList()
                    }
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    candidates = candidates,
                    error = null
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load friends"
                )
            }
        }
    }

    fun addMember(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)

            groupRepository.addMember(groupId, userId, role = "member").fold(
                onSuccess = {
                    val remaining = _uiState.value.candidates.filterNot { it.userId == userId }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        candidates = remaining,
                        successMessage = "Member added to group"
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to add member"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}
