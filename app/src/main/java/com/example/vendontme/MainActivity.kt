package com.example.vendontme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.example.vendontme.ui.navigation.Screen
import com.example.vendontme.ui.auth.SignInScreen
import com.example.vendontme.ui.auth.SignUpScreen
import com.example.vendontme.ui.home.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppNavigation()
        }
    }
}

@Composable
fun AppNavigation() {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = Screen.SignIn
    ) {
        composable<Screen.SignIn> {
            SignInScreen(nav)
        }

        composable<Screen.SignUp> {
            SignUpScreen(nav)
        }

        composable<Screen.Home> {
            HomeScreen()
        }
    }
}