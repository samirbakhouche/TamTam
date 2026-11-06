package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.MessageEntity
import com.example.ui.navigation.HomeTab
import com.example.ui.theme.DarkIncomingBubble
import com.example.ui.theme.LightIncomingBubble
import com.example.ui.theme.TamTamBluePrimary
import com.example.ui.theme.TamTamCyanAccent
import com.example.ui.theme.TamTamEmeraldOnline

@Composable
fun AvatarBadge(
    avatarUrl: String?,
    name: String,
    isOnline: Boolean = false,
    size: Dp = 52.dp,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.size(size)) {
        if (!avatarUrl.isNullOrEmpty()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = name,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
        } else {
            val initials = if (name.isNotBlank()) name.take(2).uppercase() else "TT"
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(TamTamBluePrimary, TamTamCyanAccent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.38f).sp
                )
            }
        }

        if (isOnline) {
            Box(
                modifier = Modifier
                    .size(size * 0.28f)
                    .clip(CircleShape)
                    .background(TamTamEmeraldOnline)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
fun StoryCircle(
    name: String,
    avatarUrl: String?,
    isAddStory: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(68.dp)
                .then(
                    if (!isAddStory) {
                        Modifier.border(
                            2.5.dp,
                            Brush.sweepGradient(listOf(TamTamCyanAccent, TamTamBluePrimary, Color(0xFF9333EA), TamTamCyanAccent)),
                            CircleShape
                        )
                    } else {
                        Modifier.border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    }
                )
                .padding(3.dp)
        ) {
            AvatarBadge(avatarUrl = avatarUrl, name = name, size = 58.dp)
            if (isAddStory) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(TamTamBluePrimary)
                        .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isAddStory) "قصتك" else name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(66.dp)
        )
    }
}

@Composable
fun ChatBubble(
    message: MessageEntity,
    modifier: Modifier = Modifier
) {
    val isOutgoing = message.isOutgoing
    val bubbleColor = if (isOutgoing) {
        TamTamBluePrimary
    } else {
        if (MaterialTheme.colorScheme.background.value == 0xFF0B0F19.toULong()) DarkIncomingBubble else LightIncomingBubble
    }
    val textColor = if (isOutgoing) Color.White else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = if (isOutgoing) androidx.compose.foundation.layout.Arrangement.End else androidx.compose.foundation.layout.Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isOutgoing) 16.dp else 2.dp,
                bottomEnd = if (isOutgoing) 2.dp else 16.dp
            ),
            color = bubbleColor,
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = message.content,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.createdAt,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOutgoing) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                    if (isOutgoing) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (message.deliveryStatus == "read") Icons.Default.DoneAll else Icons.Default.Check,
                            contentDescription = message.deliveryStatus,
                            tint = if (message.deliveryStatus == "read") TamTamCyanAccent else Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TamTamTopAppBar(
    title: String,
    subtitle: String? = null,
    onLanguageToggle: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TamTamBluePrimary),
                contentAlignment = Alignment.Center
            ) {
                Text("ط", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TamTamEmeraldOnline,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            IconButton(
                onClick = onLanguageToggle,
                modifier = Modifier.testTag("lang_toggle_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Toggle Language",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TamTamBottomNav(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    unreadChatsCount: Int = 2,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = modifier
    ) {
        NavigationBarItem(
            selected = selectedTab == HomeTab.CHATS,
            onClick = { onTabSelected(HomeTab.CHATS) },
            icon = {
                if (unreadChatsCount > 0) {
                    BadgedBox(badge = { Badge { Text("$unreadChatsCount") } }) {
                        Icon(Icons.Default.Chat, contentDescription = "Chats")
                    }
                } else {
                    Icon(Icons.Default.Chat, contentDescription = "Chats")
                }
            },
            label = { Text("المحادثات", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TamTamBluePrimary,
                indicatorColor = TamTamBluePrimary.copy(alpha = 0.15f)
            )
        )

        NavigationBarItem(
            selected = selectedTab == HomeTab.GROUPS,
            onClick = { onTabSelected(HomeTab.GROUPS) },
            icon = { Icon(Icons.Default.Group, contentDescription = "Groups") },
            label = { Text("المجموعات", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TamTamBluePrimary,
                indicatorColor = TamTamBluePrimary.copy(alpha = 0.15f)
            )
        )

        NavigationBarItem(
            selected = selectedTab == HomeTab.STORIES,
            onClick = { onTabSelected(HomeTab.STORIES) },
            icon = { Icon(Icons.Default.Star, contentDescription = "Stories") },
            label = { Text("القصص", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TamTamBluePrimary,
                indicatorColor = TamTamBluePrimary.copy(alpha = 0.15f)
            )
        )

        NavigationBarItem(
            selected = selectedTab == HomeTab.CALLS,
            onClick = { onTabSelected(HomeTab.CALLS) },
            icon = { Icon(Icons.Default.Call, contentDescription = "Calls") },
            label = { Text("المكالمات", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TamTamBluePrimary,
                indicatorColor = TamTamBluePrimary.copy(alpha = 0.15f)
            )
        )

        NavigationBarItem(
            selected = selectedTab == HomeTab.CONTACTS,
            onClick = { onTabSelected(HomeTab.CONTACTS) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Contacts") },
            label = { Text("جهات الاتصال", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TamTamBluePrimary,
                indicatorColor = TamTamBluePrimary.copy(alpha = 0.15f)
            )
        )
    }
}
