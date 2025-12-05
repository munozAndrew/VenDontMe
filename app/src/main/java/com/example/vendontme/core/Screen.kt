package com.example.vendontme.core

sealed class Screen(val route: String) {
    // Auth screens
    data object SignIn : Screen("sign_in")
    data object SignUp : Screen("sign_up")
    data object Welcome : Screen("welcome")

    // Main screens
    data object Home : Screen("home")
    data object Friends : Screen("friends")

    // Group screens
    data object CreateGroup : Screen("create_group")
    data object GroupDetail : Screen("group_detail/{groupId}") {
        fun pass(groupId: String) = "group_detail/$groupId"
    }

    // Receipt/Camera screens
    data object CaptureReceipt : Screen("capture_receipt/{groupId}") {
        fun pass(groupId: String) = "capture_receipt/$groupId"
    }

    data object AddReceiptItems : Screen("add_receipt_items/{groupId}/{imageUri}") {
        fun pass(groupId: String, imageUri: String) = "add_receipt_items/$groupId/$imageUri"
    }

    data object ReceiptDetail : Screen("receipt_detail/{receiptId}") {
        fun pass(receiptId: String) = "receipt_detail/$receiptId"
    }

    // Other screens
    data object Settings : Screen("settings")
    data object Error : Screen("error")
}