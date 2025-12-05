package com.example.vendontme.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vendontme.core.SupabaseClient
import com.example.vendontme.data.repository.*
import com.example.vendontme.ui.auth.AuthViewModel
import com.example.vendontme.ui.home.HomeViewModel
import com.example.vendontme.ui.friends.FriendViewModel
import com.example.vendontme.ui.receipt.ReceiptViewModel
import com.example.vendontme.ui.groups.GroupDetailViewModel
import com.example.vendontme.ui.receipt.ReceiptDetailViewModel

object AppModule {

    // Supabase Client (Singleton)
    private val supabaseClient by lazy {
        SupabaseClient
    }

    // Repositories (Singletons)
    private var authRepository: AuthRepository? = null
    private var groupRepository: GroupRepository? = null
    private var friendRepository: FriendRepository? = null
    private var receiptRepository: ReceiptRepository? = null

    fun provideAuthRepository(): AuthRepository {
        if (authRepository == null) {
            authRepository = SupabaseAuthRepository(supabaseClient)
        }
        return authRepository!!
    }

    fun provideFriendRepository(): FriendRepository {
        if (friendRepository == null) {
            friendRepository = SupabaseFriendRepository(supabaseClient)
        }
        return friendRepository!!
    }

    fun provideGroupRepository(): GroupRepository {
        if (groupRepository == null) {
            groupRepository = GroupRepository()
        }
        return groupRepository!!
    }

    fun provideReceiptRepository(): ReceiptRepository {
        if (receiptRepository == null) {
            receiptRepository = SupabaseReceiptRepository(supabaseClient)
        }
        return receiptRepository!!
    }

    // ViewModels (Factory Methods)

    fun provideAuthViewModelFactory(): ViewModelProvider.Factory {
        val repo = provideAuthRepository()
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(repo) as T
            }
        }
    }

    fun provideFriendViewModelFactory(): ViewModelProvider.Factory {
        val friendRepo = provideFriendRepository()
        val authRepo = provideAuthRepository()
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FriendViewModel(friendRepo, authRepo) as T
            }
        }
    }

    fun provideHomeViewModelFactory(): ViewModelProvider.Factory {
        val groupRepo = provideGroupRepository()
        val authRepo = provideAuthRepository()
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(groupRepo, authRepo) as T
            }
        }
    }

    fun provideReceiptViewModelFactory(context: Context): ViewModelProvider.Factory {
        val receiptRepo = provideReceiptRepository()
        val authRepo = provideAuthRepository()
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ReceiptViewModel(receiptRepo, authRepo, context) as T
            }
        }
    }

    fun provideGroupDetailViewModelFactory(groupId: String): ViewModelProvider.Factory {
        val groupRepo = provideGroupRepository()
        val receiptRepo = provideReceiptRepository()

        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GroupDetailViewModel(
                    groupRepository = groupRepo,
                    receiptRepository = receiptRepo,
                    groupId = groupId
                ) as T
            }
        }
    }
    fun provideReceiptDetailViewModelFactory(receiptId: String): ViewModelProvider.Factory {
        val receiptRepo = provideReceiptRepository()
        val groupRepo = provideGroupRepository()
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ReceiptDetailViewModel(receiptRepo, groupRepo, receiptId) as T
            }
        }
    }
}