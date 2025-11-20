package com.example.vendontme.data.repository

import com.example.vendontme.data.model.Profile

interface AuthRepository {

    suspend fun signup(email: String, password: String, username: String, displayName: String): Result<Profile>

    suspend fun login(email: String, password: String): Result<Profile>

    suspend fun logout(): Result<Unit>

    suspend fun isUserLoggedIn(): Boolean

    // Returns the Supabase auth user's ID, or null if not logged in
    fun getCurrentUserId(): String?

    // Returns the Profile object for the current user, or null if not logged in
    suspend fun getCurrentUser(): Profile?
}
