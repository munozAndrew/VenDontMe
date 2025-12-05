package com.example.vendontme.data.repository

import com.example.vendontme.data.model.Profile

interface AuthRepository {

    suspend fun signup(email: String, password: String, username: String, displayName: String): Result<Profile>

    suspend fun login(email: String, password: String): Result<Profile>

    suspend fun logout(): Result<Unit>

    suspend fun isUserLoggedIn(): Boolean

    fun getCurrentUserId(): String?

    suspend fun getCurrentUser(): Profile?
}
