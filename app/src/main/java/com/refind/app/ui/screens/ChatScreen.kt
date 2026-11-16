package com.refind.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.refind.app.R
import com.refind.app.data.ChatMessage
import com.refind.app.data.DemoData
import com.refind.app.data.ItemReport
import com.refind.app.ui.components.categoryIcon
import com.refind.app.ui.components.staggerIn
import com.refind.app.ui.theme.Blue100
import com.refind.app.ui.theme.Blue600
import com.refind.app.ui.theme.Blue900

/** Verification chat between the owner and the finder. */
@Composable
fun ChatScreen(
    initialMessages: List<ChatMessage>,
    item: ItemReport,
    peerName: String,
    peerSubtitle: String,
    peerOnline: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var messages by remember {
        mutableStateOf(initialMessages)
    }
    var draft by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = modifier.fillMaxSize().statusBarsPadding().imePadding()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            Image(
                painter = painterResource(R.drawable.avatar_default),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape),
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = peerName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (peerOnline) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                        )
                    }
                }
                Text(
                    text = peerSubtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Context card: which item this chat is about + the handover plan.
        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        ) {
            Column(
                Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        categoryIcon(item.category),
                        contentDescription = null,
                        tint = Blue600,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Chatting about: ${item.title} (your report)",
                        style = MaterialTheme.typography.labelMedium,
                        color = Blue900,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${(DemoData.MATCH_CONFIDENCE * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = Blue600,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Star,
                        contentDescription = null,
                        tint = Blue600,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Safe handover point: Library Cafe, 3:00 PM",
                        style = MaterialTheme.typography.labelMedium,
                        color = Blue900,
                    )
                }
            }
        }

        // Messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp, vertical = 14.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(messages, key = { _, m -> m.id }) { index, message ->
                when {
                    message.isSystem -> SystemBubble(message, index)
                    message.isMe -> MyBubble(message, index)
                    else -> TheirBubble(message, index)
                }
            }

        }

        // Composer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message…") },
                shape = RoundedCornerShape(24.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (draft.isNotBlank()) {
                        messages = messages + ChatMessage(
                            id = (messages.maxOfOrNull { it.id } ?: 0) + 1,
                            text = draft.trim(),
                            isMe = true,
                            time = "now",
                        )
                        draft = ""
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun SystemBubble(message: ChatMessage, index: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .staggerIn(index, stepMs = 90),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message.text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun MyBubble(message: ChatMessage, index: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .staggerIn(index, stepMs = 90),
        horizontalArrangement = Arrangement.End,
    ) {
        Bubble(
            text = message.text,
            container = MaterialTheme.colorScheme.primary,
            content = MaterialTheme.colorScheme.onPrimary,
            time = message.time,
            modifier = Modifier.widthIn(max = 280.dp),
        )
    }
}

@Composable
private fun TheirBubble(message: ChatMessage, index: Int) {
    val isVerification = message.text.startsWith("Verification question")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .staggerIn(index, stepMs = 90),
        horizontalArrangement = Arrangement.Start,
    ) {
        Bubble(
            text = message.text,
            container = if (isVerification) {
                Blue100
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            content = if (isVerification) Blue900 else MaterialTheme.colorScheme.onSurface,
            leadingIcon = if (isVerification) {
                { Icon(Icons.Outlined.Lock, contentDescription = null, tint = Blue600, modifier = Modifier.size(14.dp)) }
            } else {
                null
            },
            time = message.time,
            modifier = Modifier.widthIn(max = 280.dp),
        )
    }
}

/**
 * WhatsApp-style bubble: the send time sits as small muted text pinned to
 * the bottom-right of the bubble, next to the last line of the message.
 */
@Composable
private fun Bubble(
    text: String,
    container: androidx.compose.ui.graphics.Color,
    content: androidx.compose.ui.graphics.Color,
    time: String? = null,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    Surface(
        color = container,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier,
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                leadingIcon?.invoke()
                leadingIcon?.let { Spacer(Modifier.width(8.dp)) }
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = content,
                )
            }
            time?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = content.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 2.dp),
                )
            }
        }
    }
}
