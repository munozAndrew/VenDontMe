package com.example.vendontme.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vendontme.data.repository.AuthRepository
import com.example.vendontme.data.repository.GroupRepository
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

            val result = groupRepository.getGroupsForUser(userId)

            if (result.isFailure) {
                _uiState.value = HomeUiState(isLoading = false, groups = emptyList())
                createError = result.exceptionOrNull()?.message
                return@launch
            }

            val groups = result.getOrNull() ?: emptyList()

            // Fetch member counts
            val groupUiList = groups.map { group ->
                val countResult = groupRepository.getMemberCount(group.id ?: "")
                val memberCount = countResult.getOrNull() ?: 0

                GroupUi(
                    id = group.id ?: "",
                    name = group.name ?: "Unnamed Group",
                    description = group.description,
                    avatarUrl = group.avatarUrl,
                    membersCount = memberCount
                )
            }

            _uiState.value = HomeUiState(
                isLoading = false,
                groups = groupUiList
            )
        }
    }

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

            val result = groupRepository.createGroup(
                name = name,
                description = description,
                createdBy = userId
            )

            if (result.isFailure) {
                createError = result.exceptionOrNull()?.message ?: "Failed to create group"
                isCreatingGroup = false
                return@launch
            }

            val createdGroup = result.getOrNull()
            val groupId = createdGroup?.id ?: ""

            // Refresh groups list
            loadGroups()

            isCreatingGroup = false

            // Navigate to new group
            onCreated(groupId)
        }
    }
}
