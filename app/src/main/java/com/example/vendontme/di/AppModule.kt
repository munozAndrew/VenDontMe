package com.example.vendontme.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vendontme.core.SupabaseClient
import com.example.vendontme.data.repository.AuthRepository
import com.example.vendontme.data.repository.FriendRepository
import com.example.vendontme.data.repository.GroupRepository
import com.example.vendontme.data.repository.ReceiptRepository
import com.example.vendontme.data.repository.SupabaseAuthRepository
import com.example.vendontme.data.repository.SupabaseFriendRepository
import com.example.vendontme.data.repository.SupabaseGroupRepository
import com.example.vendontme.data.repository.SupabaseReceiptRepository
import com.example.vendontme.ui.auth.AuthViewModel
import com.example.vendontme.ui.friends.FriendViewModel
import com.example.vendontme.ui.groups.AddGroupMembersViewModel
import com.example.vendontme.ui.groups.GroupDetailViewModel
import com.example.vendontme.ui.home.HomeViewModel
import com.example.vendontme.ui.receipt.ReceiptDetailViewModel
import com.example.vendontme.ui.receipt.ReceiptViewModel

object AppModule {

    private val supabaseClient by lazy { SupabaseClient }

    private var authRepository: AuthRepository? = null
    private var friendRepository: FriendRepository? = null
    private var groupRepository: GroupRepository? = null
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
            groupRepository = SupabaseGroupRepository(supabaseClient)
        }
        return groupRepository!!
    }

    fun provideReceiptRepository(): ReceiptRepository {
        if (receiptRepository == null) {
            receiptRepository = SupabaseReceiptRepository(supabaseClient)
        }
        return receiptRepository!!
    }

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
                return ReceiptViewModel(
                    receiptRepository = receiptRepo,
                    authRepository = authRepo,
                    context = context
                ) as T
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
                return ReceiptDetailViewModel(
                    receiptRepository = receiptRepo,
                    groupRepository = groupRepo,
                    receiptId = receiptId
                ) as T
            }
        }
    }

    fun provideAddGroupMembersViewModelFactory(groupId: String): ViewModelProvider.Factory {
        val groupRepo = provideGroupRepository()
        val friendRepo = provideFriendRepository()
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AddGroupMembersViewModel(
                    groupRepository = groupRepo,
                    friendRepository = friendRepo,
                    groupId = groupId
                ) as T
            }
        }
    }
}
