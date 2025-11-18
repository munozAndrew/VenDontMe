package com.example.vendontme.di

import com.example.vendontme.data.remote.SupabaseClient
import com.example.vendontme.data.repository.AuthRepository
import com.example.vendontme.data.repository.SupabaseAuthRepository
import io.github.jan.supabase.auth.Auth

object AppModule {

    private val supabaseClient: SupabaseClient by lazy {
        SupabaseClient
    }

    //Auth repo
    private var authRepository: AuthRepository? = null


    fun provideAuthRepo(): AuthRepository {

        if (authRepository == null) {
            authRepository = SupabaseAuthRepository(supabaseClient)
        }

        return authRepository!!
    }

    //group repo
    //profiles repo
    //storage repo
    //etc




}