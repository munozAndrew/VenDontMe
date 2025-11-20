package com.example.vendontme.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vendontme.data.repository.GroupRepository
import com.example.vendontme.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val groupRepository: GroupRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    var isCreatingGroup by mutableStateOf(false)
        private set

    var createError by mutableStateOf<String?>(null)
        private set

    init {
        loadGroups()
    }

    fun loadGroups() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.value = HomeUiState(isLoading = false, groups = emptyList())
                return@launch
            }

            try {
                val groups = groupRepository.getGroupsForUser(userId)

                _uiState.value = HomeUiState(
                    isLoading = false,
                    groups = groups.map { group ->
                        GroupUi(
                            id = group.id ?: "",
                            name = group.name,
                            description = group.description,
                            avatarUrl = group.avatarUrl,
                            membersCount = groupRepository.getMemberCount(group.id!!)
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState(
                    isLoading = false,
                    groups = emptyList()
                )
                createError = e.message
            }
        }
    }

    /**
     * Create a new group with the current user as admin
     * @param onCreated callback with the created group ID
     */
    fun createGroup(name: String, description: String?, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            isCreatingGroup = true
            createError = null

            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                createError = "Not logged in"
                isCreatingGroup = false
                return@launch
            }

            try {
                // Create group and auto-add creator as admin
                val group = groupRepository.createGroup(
                    name = name,
                    description = description,
                    createdBy = userId
                )

                // Reload groups to show the new one
                loadGroups()

                isCreatingGroup = false

                // Navigate to the new group
                onCreated(group.id ?: "")

            } catch (e: Exception) {
                createError = e.message ?: "Failed to create group"
                isCreatingGroup = false
            }
        }
    }
}