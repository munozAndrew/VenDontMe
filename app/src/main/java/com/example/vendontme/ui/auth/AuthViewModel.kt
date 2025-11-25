package com.example.vendontme.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.vendontme.ui.navigation.Screen
import com.example.vendontme.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun signIn(email: String, password: String, nav: NavController) {
        viewModelScope.launch {
            try {
                loading = true
                error = null

                val result = authRepository.login(email, password)

                if (result.isSuccess) {
                    nav.navigate(Screen.Home) {
                        popUpTo(Screen.SignIn) { inclusive = true }
                    }
                } else {
                    error = result.exceptionOrNull()?.message
                }
            } catch (e: Exception) {
                error = e.message ?: "Sign in failed"
            } finally {
                loading = false
            }
        }
    }

    fun signUp(email: String, password: String, username: String, displayName: String, nav: NavController) {
        viewModelScope.launch {
            try {
                loading = true
                error = null

                val result = authRepository.signup(email, password, username, displayName)

                if (result.isSuccess) {
                    nav.navigate(Screen.SignIn) {
                        popUpTo(Screen.SignUp) { inclusive = true }
                    }
                } else {
                    error = result.exceptionOrNull()?.message ?: "Sign up failed"
                }
            } catch (e: Exception) {
                error = e.message ?: "Sign up failed"
            } finally {
                loading = false
            }
        }
    }

    fun checkSession(nav: NavController) {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()

                if (user != null) {
                    nav.navigate(Screen.Home) {
                        popUpTo(Screen.SignIn) { inclusive = true }
                    }
                }
            } catch (e: Exception) {
                // User not logged in, stay on sign in screen
            }
        }
    }
}