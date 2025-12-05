package com.example.vendontme.data.repository

import com.example.vendontme.core.SupabaseClient
import com.example.vendontme.data.model.Friend
import com.example.vendontme.data.model.Profile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.postgrest


class SupabaseFriendRepository(
    private val supabaseClient: SupabaseClient
) : FriendRepository {

    private val supabase = supabaseClient.client

    override suspend fun sendFriendRequest(friendId: String): Result<Friend> {
        return try {
            val currentUserId = supabase.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("User not authenticated"))

            // Check if friendship already exists
            val existing = supabase.from("friends")
                .select {
                    filter {
                        or {
                            and {
                                eq("user_id", currentUserId)
                                eq("friend_id", friendId)
                            }
                            and {
                                eq("user_id", friendId)
                                eq("friend_id", currentUserId)
                            }
                        }
                    }
                }
                .decodeSingleOrNull<Friend>()

            if (existing != null) {
                return Result.failure(Exception("Friend request already exists"))
            }

            val friend = Friend(
                userId = currentUserId,
                friendId = friendId,
                status = "pending"
            )

            val inserted = supabase.from("friends")
                .insert(friend)
                .decodeSingle<Friend>()

            Result.success(inserted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun acceptFriendRequest(friendshipId: String): Result<Friend> {
        return try {
            val updated = supabase.from("friends")
                .update({
                    set("status", "accepted")
                }) {
                    filter {
                        eq("id", friendshipId)
                    }
                    select()
                }
                .decodeSingle<Friend>()

            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun rejectFriendRequest(friendshipId: String): Result<Unit> {
        return try {
            supabase.from("friends")
                .delete {
                    filter {
                        eq("id", friendshipId)
                    }
                }
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun blockUser(friendshipId: String): Result<Friend> {
        return try {
            val updated = supabase.from("friends")
                .update({
                    set("status", "blocked")
                }) {
                    filter {
                        eq("id", friendshipId)
                    }
                }
                .decodeSingle<Friend>()

            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun getFriends(): Result<List<Friend>> {
        return try {
            val currentUserId = supabase.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("User not authenticated"))

            val friends = supabase.from("friends")
                .select {
                    filter {
                        or {
                            eq("user_id", currentUserId)
                            eq("friend_id", currentUserId)
                        }
                        eq("status", "accepted")
                    }
                }
                .decodeList<Friend>()

            Result.success(friends)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPendingRequests(): Result<List<Friend>> {
        return try {
            val currentUserId = supabase.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("User not authenticated"))

            // Get requests where current user is the friend_id (received requests)
            val requests = supabase.from("friends")
                .select {
                    filter {
                        eq("friend_id", currentUserId)
                        eq("status", "pending")
                    }
                }
                .decodeList<Friend>()

            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSentRequests(): Result<List<Friend>> {
        return try {
            val currentUserId = supabase.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("User not authenticated"))

            // Get requests where current user is the user_id (sent requests)
            val requests = supabase.from("friends")
                .select {
                    filter {
                        eq("user_id", currentUserId)
                        eq("status", "pending")
                    }
                }
                .decodeList<Friend>()

            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchUsersByUsername(query: String): Result<List<Profile>> {
        return try {
            val currentUserId = supabase.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("User not authenticated"))

            val profiles = supabase.from("profiles")
                .select {
                    filter {
                        ilike("username", "%$query%")
                        neq("id", currentUserId) // Exclude current user
                    }
                }
                .decodeList<Profile>()

            Result.success(profiles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFriendshipStatus(friendId: String): Result<Friend?> {
        return try {
            val currentUserId = supabase.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("User not authenticated"))

            val friendship = supabase.from("friends")
                .select {
                    filter {
                        or {
                            and {
                                eq("user_id", currentUserId)
                                eq("friend_id", friendId)
                            }
                            and {
                                eq("user_id", friendId)
                                eq("friend_id", currentUserId)
                            }
                        }
                    }
                }
                .decodeSingleOrNull<Friend>()

            Result.success(friendship)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFriend(friendshipId: String): Result<Unit> {
        return try {
            supabase.from("friends")
                .delete {
                    filter {
                        eq("id", friendshipId)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}