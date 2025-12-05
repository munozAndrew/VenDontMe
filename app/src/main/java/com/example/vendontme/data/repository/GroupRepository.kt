package com.example.vendontme.data.repository

import com.example.vendontme.data.model.Group
import com.example.vendontme.data.model.GroupMember
import com.example.vendontme.data.model.Profile

interface GroupRepository {

    suspend fun getGroupById(groupId: String): Result<Group>
    suspend fun getGroupsForUser(userId: String): Result<List<Group>>

    suspend fun createGroup(name: String, description: String?, createdBy: String): Result<Group>
    suspend fun updateGroup(groupId: String, name: String?, description: String?, avatarUrl: String?): Result<Unit>
    suspend fun deleteGroup(groupId: String): Result<Unit>

    suspend fun getGroupMembers(groupId: String): Result<List<Pair<GroupMember, Profile>>>

    // Updated return type: returns BOTH GroupMember + Profile
    suspend fun addMember(groupId: String, userId: String, role: String = "member"): Result<Pair<GroupMember, Profile>>

    suspend fun removeMember(groupId: String, userId: String): Result<Unit>

    suspend fun getMemberCount(groupId: String): Result<Int>
    suspend fun isUserAdmin(groupId: String, userId: String): Result<Boolean>
}
