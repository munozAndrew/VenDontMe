package com.example.vendontme.ui.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vendontme.data.model.Friend
import com.example.vendontme.data.model.Profile
import com.example.vendontme.data.repository.AuthRepository
import com.example.vendontme.data.repository.FriendRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FriendUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val friends: List<Friend> = emptyList(),
    val pendingRequests: List<Friend> = emptyList(),
    val sentRequests: List<Friend> = emptyList(),
    val searchResults: List<Profile> = emptyList(),
    val searchQuery: String = "",
    val successMessage: String? = null
)

class FriendViewModel(
    private val friendRepository: FriendRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendUiState())
    val uiState: StateFlow<FriendUiState> = _uiState.asStateFlow()

    init {
        loadFriends()
        loadPendingRequests()
        loadSentRequests()
    }

    fun loadFriends() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            friendRepository.getFriends().fold(
                onSuccess = { friends ->
                    _uiState.value = _uiState.value.copy(
                        friends = friends,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message,
                        isLoading = false
                    )
                }
            )
        }
    }

    fun loadPendingRequests() {
        viewModelScope.launch {
            friendRepository.getPendingRequests().fold(
                onSuccess = { requests ->
                    _uiState.value = _uiState.value.copy(pendingRequests = requests)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
            )
        }
    }

    fun loadSentRequests() {
        viewModelScope.launch {
            friendRepository.getSentRequests().fold(
                onSuccess = { requests ->
                    _uiState.value = _uiState.value.copy(sentRequests = requests)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
            )
        }
    }

    fun searchUsers(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                searchQuery = query,
                isLoading = true,
                error = null
            )

            if (query.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    searchResults = emptyList(),
                    isLoading = false
                )
                return@launch
            }

            friendRepository.searchUsersByUsername(query).fold(
                onSuccess = { profiles ->
                    _uiState.value = _uiState.value.copy(
                        searchResults = profiles,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message,
                        isLoading = false
                    )
                }
            )
        }
    }

    fun sendFriendRequest(friendId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            friendRepository.sendFriendRequest(friendId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Friend request sent!",
                        isLoading = false
                    )
                    loadSentRequests()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to send friend request",
                        isLoading = false
                    )
                }
            )
        }
    }

    fun acceptFriendRequest(friendshipId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            friendRepository.acceptFriendRequest(friendshipId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Friend request accepted!",
                        isLoading = false
                    )
                    loadFriends()
                    loadPendingRequests()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to accept friend request",
                        isLoading = false
                    )
                }
            )
        }
    }

    fun rejectFriendRequest(friendshipId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            friendRepository.rejectFriendRequest(friendshipId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Friend request rejected",
                        isLoading = false
                    )
                    loadPendingRequests()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to reject friend request",
                        isLoading = false
                    )
                }
            )
        }
    }

    fun removeFriend(friendshipId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            friendRepository.removeFriend(friendshipId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Friend removed",
                        isLoading = false
                    )
                    loadFriends()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to remove friend",
                        isLoading = false
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

    fun clearSearchResults() {
        _uiState.value = _uiState.value.copy(
            searchResults = emptyList(),
            searchQuery = ""
        )
    }
}