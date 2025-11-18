package com.example.vendontme.data.repository

import com.example.vendontme.data.model.Profile
import com.example.vendontme.data.remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

class SupabaseAuthRepository (
    private val supabaseClient: SupabaseClient
) : AuthRepository {
    override suspend fun signup(
        email: String,
        password: String,
        username: String
    ): Result<Profile> {
        supabaseClient.client.auth.signUpWith(Email){
            this.email = email
            this.password = password
            //this.data packageJson(Add Username Here)
        }
        TODO("Not yet implemented")
        //create Profile NOW - only creates user
        //return profile
        //use try catch
    }

    override suspend fun getCurrentUser(
        email: String,
        password: String,
        username: String
    ): Profile {
        TODO("Not yet implemented")
    }

    override suspend fun isUserLoggedIn(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun login(email: String, password: String): Result<Profile> {
        TODO("Not yet implemented")
    }

    override suspend fun logout(): Result<Unit> {
        TODO("Not yet implemented")
    }

}

