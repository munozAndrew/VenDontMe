package com.example.vendontme.data.repository

import com.example.vendontme.core.SupabaseClient
import com.example.vendontme.data.model.Group
import com.example.vendontme.data.model.GroupMember
import com.example.vendontme.data.model.Profile
import io.github.jan.supabase.postgrest.postgrest
import java.util.UUID

class SupabaseGroupRepository(
    private val supabaseClient: SupabaseClient
) : GroupRepository {

    private val postgrest = supabaseClient.client.postgrest

    override suspend fun getGroupById(groupId: String): Result<Group> = runCatching {
        postgrest["groups"]
            .select { filter { eq("id", groupId) } }
            .decodeSingle()
    }

    override suspend fun getGroupsForUser(userId: String): Result<List<Group>> = runCatching {
        val memberRecords = postgrest["group_members"]
            .select { filter { eq("user_id", userId) } }
            .decodeList<GroupMember>()

        val groupIds = memberRecords.map { it.groupId }
        if (groupIds.isEmpty()) return@runCatching emptyList()

        postgrest["groups"]
            .select { filter { isIn("id", groupIds) } }
            .decodeList()
    }


    override suspend fun createGroup(
        name: String,
        description: String?,
        createdBy: String
    ): Result<Group> = runCatching {

        val group = Group(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            createdBy = createdBy
        )

        postgrest["groups"].insert(group)

        val admin = GroupMember(
            id = UUID.randomUUID().toString(),
            groupId = group.id!!,
            userId = createdBy,
            role = "admin"
        )

        postgrest["group_members"].insert(admin)

        group
    }

    override suspend fun updateGroup(
        groupId: String,
        name: String?,
        description: String?,
        avatarUrl: String?
    ): Result<Unit> = runCatching {

        val existing = postgrest["groups"]
            .select { filter { eq("id", groupId) } }
            .decodeSingle<Group>()

        val updated = existing.copy(
            name = name ?: existing.name,
            description = description ?: existing.description,
            avatarUrl = avatarUrl ?: existing.avatarUrl
        )

        postgrest["groups"].update(updated) { filter { eq("id", groupId) } }
    }

    override suspend fun deleteGroup(groupId: String): Result<Unit> = runCatching {
        postgrest["groups"].delete { filter { eq("id", groupId) } }
    }
    override suspend fun getGroupMembers(groupId: String)
            : Result<List<Pair<GroupMember, Profile>>> = runCatching {

        val members = postgrest["group_members"]
            .select { filter { eq("group_id", groupId) } }
            .decodeList<GroupMember>()

        if (members.isEmpty()) return@runCatching emptyList()

        val profiles = postgrest["profiles"]
            .select { filter { isIn("id", members.map { it.userId }) } }
            .decodeList<Profile>()

        members.mapNotNull { member ->
            val profile = profiles.find { it.id == member.userId }
            profile?.let { member to it }
        }
    }

    override suspend fun addMember(
        groupId: String,
        userId: String,
        role: String
    ): Result<Pair<GroupMember, Profile>> = runCatching {

        // Prevent duplicate member
        val existing = postgrest["group_members"]
            .select {
                filter {
                    eq("group_id", groupId)
                    eq("user_id", userId)
                }
            }
            .decodeList<GroupMember>()

        if (existing.isNotEmpty()) {
            throw IllegalStateException("This user is already a member of the group.")
        }

        val newMember = GroupMember(
            id = UUID.randomUUID().toString(),
            groupId = groupId,
            userId = userId,
            role = role
        )

        // Insert + return
        postgrest["group_members"]
            .insert(newMember) { select() }

        // Fetch profile of the new member
        val profile = postgrest["profiles"]
            .select { filter { eq("id", userId) } }
            .decodeSingle<Profile>()

        newMember to profile
    }
    override suspend fun removeMember(groupId: String, userId: String): Result<Unit> = runCatching {
        postgrest["group_members"].delete {
            filter {
                eq("group_id", groupId)
                eq("user_id", userId)
            }
        }
    }

    override suspend fun getMemberCount(groupId: String): Result<Int> = runCatching {
        postgrest["group_members"]
            .select { filter { eq("group_id", groupId) } }
            .decodeList<GroupMember>()
            .size
    }

    override suspend fun isUserAdmin(groupId: String, userId: String): Result<Boolean> = runCatching {
        val member = postgrest["group_members"]
            .select {
                filter {
                    eq("group_id", groupId)
                    eq("user_id", userId)
                }
            }
            .decodeSingleOrNull<GroupMember>()

        member?.role == "admin"
    }
}
