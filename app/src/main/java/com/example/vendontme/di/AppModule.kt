package com.example.vendontme.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vendontme.core.SupabaseClient
import com.example.vendontme.data.repository.AuthRepository
import com.example.vendontme.data.repository.SupabaseAuthRepository
import com.example.vendontme.data.repository.GroupRepository
import com.example.vendontme.ui.auth.AuthViewModel
import com.example.vendontme.ui.home.HomeViewModel

object AppModule {

    // Supabase Client (Singleton)
    private val supabaseClient by lazy {
        SupabaseClient
    }

    // Repositories (Singletons)
    private var authRepository: AuthRepository? = null
    private var groupRepository: GroupRepository? = null

    fun provideAuthRepository(): AuthRepository {
        if (authRepository == null) {
            authRepository = SupabaseAuthRepository(supabaseClient)
        }
        return authRepository!!
    }

    fun provideGroupRepository(): GroupRepository {
        if (groupRepository == null) {
            groupRepository = GroupRepository()
        }
        return groupRepository!!
    }

    // ViewModels (Factory Methods)

    fun provideAuthViewModel(): AuthViewModel {
        return AuthViewModel(
            authRepository = provideAuthRepository()
        )
    }

    fun provideAuthViewModelFactory(): ViewModelProvider.Factory {
        val repo = provideAuthRepository()
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(repo) as T
            }
        }
    }

    fun provideHomeViewModel(): HomeViewModel {
        return HomeViewModel(
            groupRepository = provideGroupRepository(),
            authRepository = provideAuthRepository()
        )
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

    fun provideGroupDetailViewModelFactory(groupId: String): ViewModelProvider.Factory {
        val groupRepo = provideGroupRepository()
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return com.example.vendontme.ui.groups.GroupDetailViewModel(groupRepo, groupId) as T
            }
        }
    }
}