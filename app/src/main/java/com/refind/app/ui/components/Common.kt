package com.refind.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.PhoneIphone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.PostAdd
import androidx.compose.material.icons.outlined.Redeem
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.refind.app.R
import com.refind.app.ui.tr
import com.refind.app.data.Category
import com.refind.app.data.ItemReport
import com.refind.app.data.ItemStatus
import com.refind.app.ui.theme.LostOrange
import com.refind.app.ui.theme.LostOrangeContainer
import com.refind.app.ui.theme.Blue100
import com.refind.app.ui.theme.Blue500
import com.refind.app.ui.theme.Blue600
import com.refind.app.ui.theme.Blue900
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun categoryIcon(category: Category): ImageVector = when (category) {
    Category.PHONE -> Icons.Outlined.PhoneIphone
    Category.WALLET -> Icons.Outlined.AccountBalanceWallet
    Category.KEYS -> Icons.Outlined.Key
    Category.HEADPHONES -> Icons.Outlined.Headphones
    Category.BOTTLE -> Icons.Outlined.LocalDrink
    Category.BOOK -> Icons.Outlined.MenuBook
    Category.BAG -> Icons.Outlined.ShoppingBag
    Category.OTHER -> Icons.Outlined.Category
}

/** Press feedback: scale the card down slightly with a spring while pressed. */
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.97f,
): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "pressScale",
    )
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/** Delayed fade + slide-up entrance used for staggered lists. */
fun Modifier.staggerIn(index: Int, stepMs: Long = 70L): Modifier = composed {
    val alpha = remember { Animatable(0f) }
    val offset = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        delay(index * stepMs)
        launch { alpha.animateTo(1f, tween(320)) }
        offset.animateTo(0f, tween(380, easing = FastOutSlowInEasing))
    }
    this.graphicsLayer {
        this.alpha = alpha.value
        translationY = 26f * offset.value
    }
}

/** Small rounded badge for LOST / FOUND with the semantic accent colour. */
@Composable
fun StatusBadge(status: ItemStatus, modifier: Modifier = Modifier) {
    val (bg, fg, label) = if (status == ItemStatus.LOST) {
        Triple(LostOrangeContainer, LostOrange, tr(R.string.status_lost))
    } else {
        Triple(Blue100, Blue600, tr(R.string.status_found))
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(fg),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = fg,
        )
    }
}

/** One feed card: colour hero block with the category icon + meta rows. */
@Composable
fun ItemCard(
    item: ItemReport,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    index: Int = 0,
    animateIn: Boolean = true,
    isMine: Boolean = false,
) {
    val interaction = remember { MutableInteractionSource() }
    val heroBrush = Brush.linearGradient(
        colors = if (item.status == ItemStatus.LOST) {
            listOf(Color(0xFFFFE9D6), Color(0xFFFFD6AE))
        } else {
            listOf(Color(0xFFE0EDFD), Color(0xFFCBDFFB))
        },
    )
    val heroTint = if (item.status == ItemStatus.LOST) LostOrange else Blue600

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .let { if (animateIn) it.staggerIn(index) else it }
            .pressScale(interaction),
        interactionSource = interaction,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(heroBrush),
                contentAlignment = Alignment.Center,
            ) {
                // A real photo wins over the category tile when attached.
                val photo = rememberUriImage(item.photoUri)
                if (photo != null) {
                    Image(
                        bitmap = photo,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        imageVector = categoryIcon(item.category),
                        contentDescription = item.category.label,
                        tint = heroTint,
                        modifier = Modifier.size(36.dp),
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(item.status)
                    if (isMine) {
                        Spacer(Modifier.width(6.dp))
                        MineChip()
                    }
                    if (item.reward != null) {
                        Spacer(Modifier.width(6.dp))
                        RewardChip(amount = item.reward)
                    }
                }
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = item.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.width(10.dp))
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = item.timeAgo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** Small outlined "YOU" chip marking the viewer's own reports in the feed. */
@Composable
fun MineChip(modifier: Modifier = Modifier) {    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Transparent)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                shape = RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = tr(R.string.chip_you),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/** Points chip, shown when the owner set a reward. */
@Composable
fun RewardChip(amount: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Outlined.Redeem,
            contentDescription = "Reward",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(13.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = tr(R.string.reward_points, amount),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

/** The gradient "post a report" banner on top of the feed. */
@Composable
fun NewReportBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .pressScale(interaction, pressedScale = 0.98f),
        interactionSource = interaction,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Blue600, Color(0xFF60A5FA)),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(
                            Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY,
                        ),
                    ),
                )
                .padding(18.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    Icons.Outlined.PostAdd,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp),
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = tr(R.string.banner_report_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                    Text(
                        text = tr(R.string.banner_report_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

/** Lost/Found segmented switch with an animated pill for the selected side. */
@Composable
fun StatusTabs(
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
            ItemStatus.LOST to tr(R.string.tab_lost, lostCount),
            ItemStatus.FOUND to tr(R.string.tab_found, foundCount),
        ).forEach { (status, label) ->
            val isSelected = selected == status
            val bg by androidx.compose.animation.animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.surface
                } else {
                    Color.Transparent
                },
                label = "statusTabBg",
            )
            val fg by androidx.compose.animation.animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                label = "statusTabFg",
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
                            .background(if (status == ItemStatus.LOST) LostOrange else Blue600),
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(text = label, style = MaterialTheme.typography.labelLarge, color = fg)
            }
        }
    }
}
