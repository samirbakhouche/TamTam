package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Badge
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ConversationEntity
import com.example.ui.components.AvatarBadge
import com.example.ui.components.StoryCircle
import com.example.ui.components.TamTamBottomNav
import com.example.ui.components.TamTamTopAppBar
import com.example.ui.navigation.HomeTab
import com.example.ui.navigation.Screen
import com.example.ui.theme.TamTamBluePrimary
import com.example.ui.theme.TamTamCyanAccent

@Composable
fun HomeScreen(
    conversations: List<ConversationEntity>,
    selectedTab: HomeTab,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onTabSelected: (HomeTab) -> Unit,
    onConversationClick: (ConversationEntity) -> Unit,
    onSettingsClick: () -> Unit,
    onLanguageToggle: () -> Unit
) {
    Scaffold(
        topBar = {
            TamTamTopAppBar(
                title = "طمطم | TAMTAM",
                subtitle = "متصل بالخادم المباشر • WebSockets Active",
                onLanguageToggle = onLanguageToggle,
                onSettingsClick = onSettingsClick
            )
        },
        bottomBar = {
            TamTamBottomNav(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                unreadChatsCount = conversations.sumOf { it.unreadCount }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (conversations.isNotEmpty()) {
                        onConversationClick(conversations.first())
                    }
                },
                containerColor = TamTamBluePrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_new_chat")
            ) {
                Icon(Icons.Default.Edit, contentDescription = "New Chat")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("ابحث في المحادثات وجهات الاتصال...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("home_search_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    focusedBorderColor = TamTamBluePrimary
                ),
                singleLine = true
            )

            when (selectedTab) {
                HomeTab.CHATS -> {
                    ChatsTabContent(
                        conversations = conversations,
                        onConversationClick = onConversationClick
                    )
                }
                HomeTab.GROUPS -> {
                    GroupsTabContent(
                        groups = conversations.filter { it.type == "group" },
                        onConversationClick = onConversationClick
                    )
                }
                HomeTab.STORIES -> {
                    StoriesTabContent()
                }
                HomeTab.CALLS -> {
                    CallsTabContent()
                }
                HomeTab.CONTACTS -> {
                    ContactsTabContent(onStartChat = {
                        if (conversations.isNotEmpty()) onConversationClick(conversations.first())
                    })
                }
            }
        }
    }
}

@Composable
fun ChatsTabContent(
    conversations: List<ConversationEntity>,
    onConversationClick: (ConversationEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Stories horizontal bar
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "الحالات والقصص (24 ساعة)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item {
                        StoryCircle(name = "أنت", avatarUrl = null, isAddStory = true, onClick = {})
                    }
                    items(conversations) { conv ->
                        StoryCircle(
                            name = conv.title.substringBefore(" "),
                            avatarUrl = conv.avatarUrl,
                            isAddStory = false,
                            onClick = { onConversationClick(conv) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Active Chats Section Header
        item {
            Text(
                text = "جميع المحادثات",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        items(conversations) { conv ->
            ConversationRow(
                conversation = conv,
                onClick = { onConversationClick(conv) }
            )
        }
    }
}

@Composable
fun ConversationRow(
    conversation: ConversationEntity,
    onClick: () -> Unit
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("conversation_item_${conversation.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarBadge(
                avatarUrl = conversation.avatarUrl,
                name = conversation.title,
                isOnline = conversation.isOnline,
                size = 54.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = conversation.lastMessageAt,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (conversation.unreadCount > 0) TamTamBluePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.lastMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (conversation.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(containerColor = TamTamBluePrimary) {
                            Text(
                                text = "${conversation.unreadCount}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupsTabContent(
    groups: List<ConversationEntity>,
    onConversationClick: (ConversationEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "المجموعات والقنوات المشتركة",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (groups.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "لا توجد مجموعات حتى الآن",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "اضغط على زر الإنشاء لإنشاء أول مجموعة مع أصدقائك أو فريق عملك.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(groups) { group ->
                ConversationRow(conversation = group, onClick = { onConversationClick(group) })
            }
        }
    }
}

@Composable
fun StoriesTabContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(TamTamCyanAccent.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text("📸", fontSize = 36.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "القصص اليومية (Stories)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "شارك لحظاتك اليومية عبر الصور والفيديوهات والنصوص، والتي تختفي تلقائياً بعد 24 ساعة مع إمكانية معرفة المشاهدين.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun CallsTabContent() {
    val callLogs = listOf(
        Triple("أميرة منصور", "مكالمة واردة • 10:15 ص (5د)", true),
        Triple("فريق الدعم الفني", "مكالمة لم يرد عليها • أمس", false),
        Triple("سمير بخوش", "مكالمة صادرة • قبل يومين (12د)", true)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(callLogs) { (name, details, isSuccess) ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarBadge(avatarUrl = null, name = name, size = 46.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSuccess) Icons.Default.CallReceived else Icons.Default.CallMade,
                                contentDescription = null,
                                tint = if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Voice Call",
                        tint = TamTamBluePrimary,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(22.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Video Call",
                        tint = TamTamCyanAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ContactsTabContent(onStartChat: () -> Unit) {
    val contacts = listOf(
        Pair("أميرة منصور", "@amira_tech • متصل الآن"),
        Pair("سمير بخوش", "@samir_dev • مهندس أنظمة ومطور أندرويد"),
        Pair("فريق الدعم طمطم", "@tamtam_support • الحساب الرسمي"),
        Pair("أحمد الجزائري", "@ahmed_dz • آخر ظهور قبل ساعة")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(contacts) { (name, bio) ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStartChat() }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarBadge(avatarUrl = null, name = name, isOnline = bio.contains("متصل"), size = 46.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(text = bio, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        text = "مراسلة",
                        color = TamTamBluePrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
