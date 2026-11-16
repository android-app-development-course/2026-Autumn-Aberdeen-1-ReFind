package com.refind.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.refind.app.data.DemoData
import com.refind.app.data.ItemReport
import com.refind.app.data.ItemStatus
import com.refind.app.ui.components.StatusBadge
import com.refind.app.ui.components.categoryIcon
import com.refind.app.ui.components.staggerIn
import com.refind.app.ui.theme.LostOrange
import com.refind.app.ui.theme.LostOrangeContainer
import com.refind.app.ui.theme.Teal100
import com.refind.app.ui.theme.Teal700

/** Detail page for a single report. The story match exposes the match card. */
@Composable
fun ItemDetailScreen(
    item: ItemReport,
    hasMatch: Boolean,
    onBack: () -> Unit,
    onMatchClick: () -> Unit,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLost = item.status == ItemStatus.LOST
    val heroBrush = Brush.verticalGradient(
        listOf(
            if (isLost) LostOrangeContainer else Teal100,
            MaterialTheme.colorScheme.background,
        ),
    )

    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Hero
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .background(heroBrush),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                IconButton(onClick = {}) {
                    Icon(
                        Icons.Outlined.IosShare,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            StatusBadge(
                item.status,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, bottom = 10.dp),
            )
            Icon(
                categoryIcon(item.category),
                contentDescription = item.category.label,
                tint = if (isLost) LostOrange else Teal700,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 26.dp)
                    .size(72.dp),
            )
        }

        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Teal100),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = item.reporter.take(1),
                        style = MaterialTheme.typography.labelLarge,
                        color = Teal700,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = item.reporter,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "2 mutual friends · on campus",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Facts grid
            val facts = listOf(
                Triple(Icons.Outlined.Place, "Location", item.location),
                Triple(Icons.Outlined.Schedule, "Reported", item.timeAgo),
                Triple(Icons.Outlined.Palette, "Colour", item.colour.ifBlank { "—" }),
                Triple(
                    categoryIcon(item.category),
                    "Category",
                    item.category.label,
                ),
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.staggerIn(0)) {
                facts.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { (icon, label, value) ->
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            ) {
                                Column(Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            icon,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = value,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (hasMatch) {
                Spacer(Modifier.height(18.dp))
                // Story match callout
                Card(
                    onClick = onMatchClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggerIn(1),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Potential match found!",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                text = "A finder reported a similar item nearby — take a look.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onChatClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text(
                    text = if (hasMatch) "Open match chat" else "Messages",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
