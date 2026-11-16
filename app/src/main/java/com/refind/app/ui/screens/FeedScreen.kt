package com.refind.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.refind.app.R
import com.refind.app.data.ItemReport
import com.refind.app.data.ItemStatus
import com.refind.app.ui.components.ItemCard
import com.refind.app.ui.components.MatchBanner
import com.refind.app.ui.components.StatusTabs
import com.refind.app.ui.components.staggerIn
import com.refind.app.ui.theme.Teal400
import com.refind.app.ui.theme.Teal700

/** Hero band height; the search card overlaps its bottom edge. */
private val HeroBandHeight = 172.dp

/**
 * Home feed: a teal hero band carries the greeting, the white search card
 * overlaps its rounded bottom edge, then the AI-match banner, the
 * Lost/Found switch and the report cards.
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
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "hero") {
            // The hero band scrolls with the content; the search card hangs
            // over its bottom edge by 26dp (extra spacing added to the banner).
            Box {
                HeroBand(userName = userName)
                SearchCard(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 26.dp)
                        .padding(horizontal = 20.dp),
                )
            }
        }
        item(key = "match-banner") {
            // Compensate for the part of the search card hanging over the band.
            Spacer(Modifier.height(38.dp))
            MatchBanner(
                confidencePercent = matchConfidencePercent,
                onClick = onMatchClick,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .staggerIn(0),
            )
        }
        item(key = "tabs") {
            Spacer(Modifier.height(4.dp))
            StatusTabs(
                selected = selectedTab,
                lostCount = lostItems.size,
                foundCount = foundItems.size,
                onSelect = { selectedTab = it },
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .staggerIn(1),
            )
        }
        itemsIndexed(items, key = { _, item -> "item-${item.id}" }) { index, item ->
            ItemCard(
                item = item,
                onClick = { onItemClick(item) },
                modifier = Modifier.padding(horizontal = 20.dp),
                index = index + 2,
                isMine = item.reporter == userName,
            )
        }
    }
}

/** Teal gradient band with the greeting and a soft decorative circle. */
@Composable
private fun HeroBand(userName: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(HeroBandHeight)
            .statusBarsPadding()
            .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
            .background(Brush.linearGradient(listOf(Teal700, Teal400))),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Hi, $userName 👋",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Lost something? ReFind is already looking.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
            Spacer(Modifier.width(14.dp))
            // Round avatar with a white ring, echoing the reference layout.
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(2.dp)
                    .clip(CircleShape),
            ) {
                Image(
                    painter = painterResource(R.drawable.avatar_default),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

/** White search card with a round teal action button. */
@Composable
private fun SearchCard(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
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
