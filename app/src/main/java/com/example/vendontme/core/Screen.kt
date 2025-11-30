package com.example.vendontme.core

sealed class Screen(val route: String) {

    data object SignIn : Screen("sign_in")
    data object SignUp : Screen("sign_up")
    data object Welcome : Screen("welcome")

    data object Friends : Screen("friends")


    data object Home : Screen("home")
    data object CreateGroup : Screen("create_group")
    data object GroupDetail : Screen("group_detail/{groupId}") {
        fun pass(groupId: String) = "group_detail/$groupId"
    }

    data object ReceiptScan : Screen("receipt_scan/{groupId}") {
        fun pass(groupId: String) = "receipt_scan/$groupId"
    }
    data object EditReceipt : Screen("edit_receipt/{groupId}/{receiptId}") {
        fun pass(groupId: String, receiptId: String) =
            "edit_receipt/$groupId/$receiptId"
    }

    data object History : Screen("history/{groupId}") {
        fun pass(groupId: String) = "history/$groupId"
    }

    data object SplitBill : Screen("split_bill/{groupId}") {
        fun pass(groupId: String) = "split_bill/$groupId"
    }
    data object SplitSummary : Screen("split_summary/{groupId}") {
        fun pass(groupId: String) = "split_summary/$groupId"
    }
    data object Settings : Screen("settings")
    data object Camera : Screen("camera")
    data object Error : Screen("error")
}
