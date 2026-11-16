package com.refind.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.refind.app.data.DemoData
import com.refind.app.data.ItemReport
import com.refind.app.data.ItemStatus
import com.refind.app.ui.components.ItemCard
import com.refind.app.ui.components.MatchBanner
import com.refind.app.ui.components.staggerIn

/**
 * Home feed: greeting, the AI-match banner, a Lost/Found segmented switch
 * and the report cards. Cards enter with a staggered slide-up.
 */
@Composable
fun FeedScreen(
    userName: String,
    lostItems: List<ItemReport>,
    foundItems: List<ItemReport>,
    matchConfidencePercent: Int,
    onMatchClick: () -> Unit,
    onItemClick: (ItemReport) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableStateOf(ItemStatus.LOST) }
    val items = if (selectedTab == ItemStatus.LOST) lostItems else foundItems

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "greeting") {
            Column(Modifier.fillMaxWidth()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Hi, $userName 👋",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Lost something? ReFind is already looking.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
            }
        }
        item(key = "search") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Search lost & found…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(10.dp))
                // Round action button: makes the search affordance explicit.
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
        item(key = "match-banner") {
            Spacer(Modifier.height(4.dp))
            MatchBanner(
                confidencePercent = matchConfidencePercent,
                onClick = onMatchClick,
                modifier = Modifier.staggerIn(0),
            )
        }
        item(key = "tabs") {
            Spacer(Modifier.height(4.dp))
            SegmentedTabs(
                selected = selectedTab,
                lostCount = lostItems.size,
                foundCount = foundItems.size,
                onSelect = { selectedTab = it },
                modifier = Modifier.staggerIn(1),
            )
        }
        itemsIndexed(items, key = { _, item -> "item-${item.id}" }) { index, item ->
            ItemCard(
                item = item,
                onClick = { onItemClick(item) },
                index = index + 2,
                isMine = item.reporter == userName,
            )
        }
    }
}

/** Lost/Found switch with an animated pill for the selected side. */
@Composable
private fun SegmentedTabs(
    selected: ItemStatus,
    lostCount: Int,
    foundCount: Int,
    onSelect: (ItemStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
    ) {
        listOf(
            ItemStatus.LOST to "Lost ($lostCount)",
            ItemStatus.FOUND to "Found ($foundCount)",
        ).forEach { (status, label) ->
            val isSelected = selected == status
            val bg by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.surface
                } else {
                    Color.Transparent
                },
                label = "tabBg",
            )
            val fg by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                label = "tabFg",
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(bg)
                    .then(
                        if (isSelected) {
                            Modifier.border(
                                width = 1.5.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(14.dp),
                            )
                        } else {
                            Modifier
                        },
                    )
                    .clickable { onSelect(status) }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                if (status == ItemStatus.LOST) {
                                    com.refind.app.ui.theme.LostOrange
                                } else {
                                    com.refind.app.ui.theme.Teal700
                                },
                            ),
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(text = label, style = MaterialTheme.typography.labelLarge, color = fg)
            }
        }
    }
}
