package com.refind.app.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.refind.app.R
import com.refind.app.ui.tr
import com.refind.app.data.Conversation
import com.refind.app.ui.components.BrandHeaderBand
import com.refind.app.ui.components.StatusBadge
import com.refind.app.ui.components.staggerIn
import com.refind.app.ui.theme.BrandPrimary

/**
 * Inbox of chat threads. A brand header band echoes the feed hero; each row
 * opens the verification chat, and the item the conversation is about is
 * echoed with its own mini card. A soft brand watermark fills the space
 * below short lists instead of leaving bare background.
 */
@Composable
fun MessagesScreen(
    conversations: List<Conversation>,
    onOpenChat: (Conversation) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
    ) {
        BrandHeaderBand {
            Column(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp),
            ) {
                Text(
                    text = tr(R.string.messages_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                )
                Text(
                    text = tr(R.string.messages_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            // Oversized ghosted chat bubble fills the band below the subtitle.
            Icon(
                Icons.Outlined.ChatBubble,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.16f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 22.dp, y = 26.dp)
                    .size(118.dp),
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(
                start = 20.dp, end = 20.dp, top = 14.dp, bottom = 120.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            itemsIndexed(conversations, key = { _, c -> c.id }) { index, conv ->
                ConversationRow(
                    conversation = conv,
                    onClick = { onOpenChat(conv) },
                    modifier = Modifier.staggerIn(index),
                )
            }
            item(key = "brand-foot") {
                BrandFootprint()
            }
        }
    }
}

/** Soft brand watermark shown under the conversation list. */
@Composable
private fun BrandFootprint() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                tint = BrandPrimary,
                modifier = Modifier.size(36.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "ReFind",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
        )
    }
}

@Composable
private fun ConversationRow(
    conversation: Conversation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            if (conversation.useAppIcon) {
                // Official system chat: the app icon on the brand container.
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                    )
                }
            } else {
                Image(
                    painter = painterResource(R.drawable.avatar_default),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape),
                )
            }
            if (conversation.unread) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error),
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = conversation.peerName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = conversation.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            conversation.item?.let { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(item.status)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                text = conversation.preview,
                style = MaterialTheme.typography.bodyMedium,
                color = if (conversation.unread) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
