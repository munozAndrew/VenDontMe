package com.example.vendontme

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vendontme.core.Screen
import com.example.vendontme.ui.auth.SignInScreen
import com.example.vendontme.ui.auth.SignUpScreen
import com.example.vendontme.ui.friends.FriendsScreen
import com.example.vendontme.ui.groups.CreateGroupScreen
import com.example.vendontme.ui.groups.GroupDetailScreen
import com.example.vendontme.ui.home.HomeScreen
import com.example.vendontme.ui.receipt.AddReceiptItemsScreen
import com.example.vendontme.ui.receipt.CaptureReceiptScreen
import com.example.vendontme.ui.receipt.ReceiptDetailScreen
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
        composable(Screen.SignIn.route) {
            SignInScreen(nav)
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(nav)
        }

        composable(Screen.Welcome.route) {
            WelcomeScreen(nav)
        }

        // Main screens
        composable(Screen.Home.route) {
            HomeScreen(
                onGroupClick = { groupId ->
                    nav.navigate(Screen.GroupDetail.pass(groupId))
                },
                onCreateGroupClick = {
                    nav.navigate(Screen.CreateGroup.route)
                },
                onFriendsClick = {
                    nav.navigate(Screen.Friends.route)
                }
            )
        }

        composable(Screen.Friends.route) {
            FriendsScreen(nav)
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

        // Camera/Receipt screens
        composable(Screen.CaptureReceipt.route) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
                ?: return@composable
            CaptureReceiptScreen(
                groupId = groupId,
                navController = nav
            )
        }

        composable(Screen.AddReceiptItems.route) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
                ?: return@composable
            val imageUri = backStackEntry.arguments?.getString("imageUri")
                ?: return@composable

            AddReceiptItemsScreen(
                groupId = groupId,
                imageUri = Uri.decode(imageUri),
                navController = nav
            )
        }

        // Receipt detail screen
        composable(Screen.ReceiptDetail.route) { backStackEntry ->
            val receiptId = backStackEntry.arguments?.getString("receiptId")
                ?: return@composable
            ReceiptDetailScreen(
                receiptId = receiptId,
                navController = nav
            )
        }
    }
}