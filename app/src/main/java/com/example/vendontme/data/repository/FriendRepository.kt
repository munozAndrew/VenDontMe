package com.example.vendontme.data.repository

import com.example.vendontme.data.model.Friend
import com.example.vendontme.data.model.Profile

interface FriendRepository {

    suspend fun sendFriendRequest(friendId: String): Result<Friend>


    suspend fun acceptFriendRequest(friendshipId: String): Result<Friend>


    suspend fun rejectFriendRequest(friendshipId: String): Result<Unit>

    suspend fun blockUser(friendshipId: String): Result<Friend>

    suspend fun getFriends(): Result<List<Friend>>


    suspend fun getPendingRequests(): Result<List<Friend>>


    suspend fun getSentRequests(): Result<List<Friend>>


    suspend fun searchUsersByUsername(query: String): Result<List<Profile>>


    suspend fun getFriendshipStatus(friendId: String): Result<Friend?>

    suspend fun removeFriend(friendshipId: String): Result<Unit>
}