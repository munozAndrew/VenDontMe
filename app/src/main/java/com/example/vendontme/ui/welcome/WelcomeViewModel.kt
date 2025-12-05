package com.example.vendontme.ui.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vendontme.core.Screen
import kotlinx.coroutines.launch

class WelcomeViewModel : ViewModel() {

    fun onGetStarted(nav: androidx.navigation.NavController) {
        viewModelScope.launch {
            nav.navigate(Screen.Home.route) {
                popUpTo(Screen.Welcome.route) { inclusive = true }
            }
        }
    }
}
