package com.example.vendontme.data.repository

import com.example.vendontme.core.SupabaseClient
import com.example.vendontme.data.model.Group
import com.example.vendontme.data.model.GroupMember
import com.example.vendontme.data.model.Profile
import io.github.jan.supabase.postgrest.postgrest
import java.util.UUID

class GroupRepository {

    private val postgrest = SupabaseClient.client.postgrest


     //Create a new group and automatically add the creator as admin
     //Returns the created group
    suspend fun createGroup(name: String, description: String?, createdBy: String): Group {
        // 1. Create the group
        val newGroup = Group(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            createdBy = createdBy
        )

        postgrest["groups"].insert(newGroup)

        // 2. Add creator as admin member
        val adminMember = GroupMember(
            id = UUID.randomUUID().toString(),
            groupId = newGroup.id!!,
            userId = createdBy,
            role = "admin"
        )

        postgrest["group_members"].insert(adminMember)

        return newGroup
    }

    //Get all groups that a user is a member of

    suspend fun getGroupsForUser(userId: String): List<Group> {
        // Query: SELECT * FROM groups WHERE id IN (
        //   SELECT group_id FROM group_members WHERE user_id = userId
        // )

        val memberRecords = postgrest["group_members"]
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeList<GroupMember>()

        val groupIds = memberRecords.map { it.groupId }

        if (groupIds.isEmpty()) return emptyList()

        return postgrest["groups"]
            .select {
                filter {
                    isIn("id", groupIds)
                }
            }
            .decodeList<Group>()
    }

     // Get a specific group by ID

    suspend fun getGroupById(groupId: String): Group? {
        return postgrest["groups"]
            .select {
                filter {
                    eq("id", groupId)
                }
            }
            .decodeSingleOrNull<Group>()
    }


     //Get all members of a group with their profiles

    suspend fun getGroupMembers(groupId: String): List<Pair<GroupMember, Profile>> {
        // Get group_members for this group
        val members = postgrest["group_members"]
            .select {
                filter {
                    eq("group_id", groupId)
                }
            }
            .decodeList<GroupMember>()

        // Get profiles for all members
        val userIds = members.map { it.userId }

        if (userIds.isEmpty()) return emptyList()

        val profiles = postgrest["profiles"]
            .select {
                filter {
                    isIn("id", userIds)
                }
            }
            .decodeList<Profile>()

        // Match members with profiles
        return members.mapNotNull { member ->
            val profile = profiles.find { it.id == member.userId }
            profile?.let { member to it }
        }
    }

    //Get member count for a group

    suspend fun getMemberCount(groupId: String): Int {
        val members = postgrest["group_members"]
            .select {
                filter {
                    eq("group_id", groupId)
                }
            }
            .decodeList<GroupMember>()

        return members.size
    }

    //Check if user is admin of a group

    suspend fun isUserAdmin(groupId: String, userId: String): Boolean {
        val member = postgrest["group_members"]
            .select {
                filter {
                    eq("group_id", groupId)
                    eq("user_id", userId)
                }
            }
            .decodeSingleOrNull<GroupMember>()

        return member?.role == "admin"
    }

    //Add a member to a group (default role: "member")

    suspend fun addMember(groupId: String, userId: String, role: String = "member"): GroupMember {
        val newMember = GroupMember(
            id = UUID.randomUUID().toString(),
            groupId = groupId,
            userId = userId,
            role = role
        )

        postgrest["group_members"].insert(newMember)

        return newMember
    }

    //Remove a member from a group

    suspend fun removeMember(groupId: String, userId: String) {
        postgrest["group_members"].delete {
            filter {
                eq("group_id", groupId)
                eq("user_id", userId)
            }
        }
    }

    //Update member role (promote to admin, demote to member)
    suspend fun updateMemberRole(groupId: String, userId: String, newRole: String) {
        // Fetch the member
        val member = postgrest["group_members"]
            .select {
                filter {
                    eq("group_id", groupId)
                    eq("user_id", userId)
                }
            }
            .decodeSingleOrNull<GroupMember>() ?: return

        // Update role
        val updatedMember = member.copy(role = newRole)

        postgrest["group_members"].update(updatedMember) {
            filter {
                eq("id", member.id!!)
            }
        }
    }

    //Update group details (name, description, avatar)

    suspend fun updateGroup(groupId: String, name: String?, description: String?, avatarUrl: String?) {
        val group = getGroupById(groupId) ?: return

        val updatedGroup = group.copy(
            name = name ?: group.name,
            description = description ?: group.description,
            avatarUrl = avatarUrl ?: group.avatarUrl
        )

        postgrest["groups"].update(updatedGroup) {
            filter {
                eq("id", groupId)
            }
        }
    }

    //Delete a group (will cascade delete members via RLS)

    suspend fun deleteGroup(groupId: String) {
        postgrest["groups"].delete {
            filter {
                eq("id", groupId)
            }
        }
    }
}