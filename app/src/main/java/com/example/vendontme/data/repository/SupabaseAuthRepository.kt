package com.example.vendontme.data.repository

import com.example.vendontme.core.SupabaseClient
import com.example.vendontme.data.model.Profile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest

class SupabaseAuthRepository(
    private val supabaseClient: SupabaseClient
) : AuthRepository {

    private val auth get() = supabaseClient.client.auth
    private val postgrest get() = supabaseClient.client.postgrest

    override suspend fun signup(
        email: String,
        password: String,
        username: String,
        displayName: String
    ): Result<Profile> {
        return try {

            // 1. Create account
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            // 2. Supabase  DOES NOT return a user here.
            // We must now check the persisted session:
            val user = auth.currentUserOrNull()
                ?: return Result.failure(Exception("Please verify your email to finish signing up."))

            // 3. Now create profile
            val profile = Profile(
                id = user.id,
                username = username,
                displayName = displayName
            )

            postgrest["profiles"].insert(profile)

            Result.success(profile)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<Profile> {
        return try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val user = auth.currentUserOrNull()
                ?: return Result.failure(Exception("Login failed — no user found"))

            val profile = getProfileById(user.id)
                ?: return Result.failure(Exception("Profile not found"))

            Result.success(profile)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isUserLoggedIn(): Boolean {
        return auth.currentUserOrNull() != null
    }

    override fun getCurrentUserId(): String? {
        return auth.currentUserOrNull()?.id
    }

    override suspend fun getCurrentUser(): Profile? {
        val user = auth.currentUserOrNull() ?: return null
        return getProfileById(user.id)
    }

    private suspend fun getProfileById(id: String): Profile? {
        return postgrest["profiles"]
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull<Profile>()
    }
}
