package com.example.vendontme.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {

    @Serializable
    data object SignIn : Screen

    @Serializable
    data object SignUp : Screen

    @Serializable
    data object Home : Screen
}