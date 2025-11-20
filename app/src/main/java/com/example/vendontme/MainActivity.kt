package com.example.vendontme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.example.vendontme.core.Screen
import com.example.vendontme.ui.auth.SignInScreen
import com.example.vendontme.ui.auth.SignUpScreen
import com.example.vendontme.ui.groups.CreateGroupScreen
import com.example.vendontme.ui.groups.GroupDetailScreen
import com.example.vendontme.ui.home.HomeScreen
import com.example.vendontme.ui.welcome.WelcomeScreen

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
        startDestination = Screen.SignIn.route
    ) {
        // Auth screens
        composable(Screen.SignIn.route) { SignInScreen(nav) }
        composable(Screen.SignUp.route) { SignUpScreen(nav) }
        composable(Screen.Welcome.route) { WelcomeScreen(nav) }

        // Home screen
        composable(Screen.Home.route) {
            HomeScreen(
                onGroupClick = { groupId ->
                    nav.navigate(Screen.GroupDetail.pass(groupId))
                },
                onCreateGroupClick = {
                    nav.navigate(Screen.CreateGroup.route)
                }
            )
        }

        // Group screens
        composable(Screen.CreateGroup.route) {
            CreateGroupScreen(nav)
        }

        composable(Screen.GroupDetail.route) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
                ?: return@composable
            GroupDetailScreen(nav, groupId)
        }
    }
}