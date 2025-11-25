package com.example.vendontme.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vendontme.core.SupabaseClient
import com.example.vendontme.data.repository.AuthRepository
import com.example.vendontme.data.repository.SupabaseAuthRepository
import com.example.vendontme.ui.auth.AuthViewModel
object AppModule {

    // Supabase Client (Singleton)
    private val supabaseClient by lazy {
        SupabaseClient
    }

    // Repositories (Singletons)
    private var authRepository: AuthRepository? = null

    fun provideAuthRepository(): AuthRepository {
        if (authRepository == null) {
            authRepository = SupabaseAuthRepository(supabaseClient)
        }
        return authRepository!!
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
}