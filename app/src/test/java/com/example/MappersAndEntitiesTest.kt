package com.example

import com.example.data.local.ConversationEntity
import com.example.data.local.UserEntity
import com.example.data.mapper.toDto
import com.example.data.mapper.toEntity
import com.example.data.remote.ConversationDto
import com.example.data.remote.UserDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MappersAndEntitiesTest {

    @Test
    fun testUserDtoToEntityMapping() {
        val dto = UserDto(
            id = 42L,
            uuid = "u-42",
            username = "karim_dz",
            phone = "+213550123456",
            email = "karim@tamtam.dz",
            displayName = "Karim Benali",
            avatarUrl = "https://example.com/avatar.jpg",
            bio = "TAMTAM Android Developer",
            isOnline = true,
            lastSeenAt = "Just now",
            role = "admin",
            status = "active"
        )

        val entity = dto.toEntity()

        assertEquals(42L, entity.id)
        assertEquals("u-42", entity.uuid)
        assertEquals("karim_dz", entity.username)
        assertEquals("Karim Benali", entity.displayName)
        assertTrue(entity.isOnline)
        assertEquals("admin", entity.role)

        // Test roundtrip
        val mappedBack = entity.toDto()
        assertEquals(dto.id, mappedBack.id)
        assertEquals(dto.username, mappedBack.username)
    }

    @Test
    fun testConversationDtoToEntityMapping() {
        val dto = ConversationDto(
            id = 10L,
            uuid = "conv-10",
            type = "group",
            title = "TAMTAM Core Engineers",
            avatarUrl = "https://example.com/group.jpg",
            lastMessage = "MySQL & Room architecture complete!",
            lastMessageAt = "10:30 AM",
            unreadCount = 5,
            isPinned = true,
            isMuted = false,
            isArchived = false,
            isOnline = true,
            recipientId = null
        )

        val entity = dto.toEntity()

        assertEquals(10L, entity.id)
        assertEquals("conv-10", entity.uuid)
        assertEquals("group", entity.type)
        assertEquals("TAMTAM Core Engineers", entity.title)
        assertEquals(5, entity.unreadCount)
        assertTrue(entity.isPinned)

        // Test roundtrip
        val mappedBack = entity.toDto()
        assertEquals(dto.title, mappedBack.title)
        assertEquals(dto.unreadCount, mappedBack.unreadCount)
    }
}
