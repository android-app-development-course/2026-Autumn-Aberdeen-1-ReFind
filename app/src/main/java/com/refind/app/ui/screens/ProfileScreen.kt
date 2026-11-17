package com.refind.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.refind.app.AppInfo
import com.refind.app.R
import com.refind.app.ui.tr
import com.refind.app.data.DemoData
import com.refind.app.data.ItemStatus
import com.refind.app.ui.AppLanguage
import com.refind.app.data.ItemReport
import com.refind.app.ui.components.categoryIcon
import com.refind.app.ui.theme.BrandContainer
import com.refind.app.ui.theme.BrandPrimary
import com.refind.app.ui.theme.BrandInk
import kotlin.math.roundToInt

/** Profile tab: identity, animated stats, stacked report preview, settings. */
@Composable
fun ProfileScreen(
    myReports: List<ItemReport>,
    onOpenReports: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var notificationsOn by remember { mutableStateOf(true) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    // Which language the app is currently showing (in-app state, no recreation).
    val languageLabel = if (AppLanguage.current == "zh") "中文" else "English"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // Identity header: banner image with a teal scrim blending into the page
        Box {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_profile_banner),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                // Blue wash keeps the brand readable and blends into the page below.
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(BrandPrimary.copy(alpha = 0.55f), BrandPrimary.copy(alpha = 0.35f)),
                            ),
                        ),
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(122.dp))
                Image(
                    painter = painterResource(R.drawable.avatar_default),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .shadow(6.dp, CircleShape),
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Peter Chen",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = tr(R.string.items_reunited, 12),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // Animated stat row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatCard(value = myReports.size, label = tr(R.string.stat_reports), modifier = Modifier.weight(1f))
            StatCard(value = 1, label = tr(R.string.stat_matches), modifier = Modifier.weight(1f))
            StatCard(value = 180, label = tr(R.string.stat_points), modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(22.dp))
        Text(
            text = "  " + tr(R.string.label_my_reports),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Spacer(Modifier.height(10.dp))
        StackedReportsPreview(
            latest = myReports.firstOrNull(),
            totalCount = myReports.size,
            lostCount = myReports.count { it.status == ItemStatus.LOST },
            foundCount = myReports.count { it.status == ItemStatus.FOUND },
            onClick = onOpenReports,
        )

        Spacer(Modifier.height(22.dp))
        Text(
            text = "  " + tr(R.string.label_settings),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Spacer(Modifier.height(10.dp))
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SettingsRow(
                icon = { Icon(Icons.Outlined.Notifications, contentDescription = null, tint = BrandPrimary) },
                title = tr(R.string.match_notifications),
            ) {
                Switch(checked = notificationsOn, onCheckedChange = { notificationsOn = it })
            }
            SettingsRow(
                icon = { Icon(Icons.Outlined.Info, contentDescription = null, tint = BrandPrimary) },
                title = tr(R.string.about_refind),
                onClick = { showAboutDialog = true },
            ) {
                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            SettingsRow(
                icon = { Icon(Icons.Outlined.Language, contentDescription = null, tint = BrandPrimary) },
                title = tr(R.string.language),
                onClick = { showLanguageDialog = true },
            ) {
                Text(
                    text = languageLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            SettingsRow(
                icon = {
                    Icon(
                        Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
                title = tr(R.string.sign_out),
                titleColor = MaterialTheme.colorScheme.error,
                onClick = { showSignOutDialog = true },
            ) {
                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (showLanguageDialog) {
            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                title = { Text(tr(R.string.pick_language)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = {
                                showLanguageDialog = false
                                AppLanguage.set(context, "en")
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                tr(R.string.language_english),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        TextButton(
                            onClick = {
                                showLanguageDialog = false
                                AppLanguage.set(context, "zh")
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                tr(R.string.language_chinese),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showLanguageDialog = false }) {
                        Text(tr(R.string.action_cancel))
                    }
                },
            )
        }

        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text(tr(R.string.app_name)) },
                text = {
                    Text("Version " + com.refind.app.AppInfo.VERSION + "\n\n" + tr(R.string.about_body))
                },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) {
                        Text(tr(R.string.action_close))
                    }
                },
            )
        }

        if (showSignOutDialog) {
            AlertDialog(
                onDismissRequest = { showSignOutDialog = false },
                title = { Text(tr(R.string.sign_out_title)) },
                text = {
                    Text(tr(R.string.sign_out_desc))
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showSignOutDialog = false
                            onSignOut()
                        },
                    ) {
                        Text(tr(R.string.sign_out), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSignOutDialog = false }) {
                        Text(tr(R.string.action_cancel))
                    }
                },
            )
        }

        Spacer(Modifier.height(30.dp))
        Text(
            text = "ReFind v" + com.refind.app.AppInfo.VERSION,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(110.dp))
    }
}

/** Number counts up from zero when the screen opens. */
@Composable
private fun StatCard(    value: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        anim.animateTo(1f, tween(850, easing = FastOutSlowInEasing))
    }
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = (value * anim.value).roundToInt().toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = BrandPrimary,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsRow(
    icon: @Composable () -> Unit,
    title: String,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit = {},
    trailing: @Composable () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon()
            Spacer(Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor,
                modifier = Modifier.weight(1f),
            )
            trailing()
        }
    }
}

/**
 * Stacked-card preview of the user's reports: two peeking edges hint at the
 * pile, the top card summarises the latest report; tapping opens the full
 * My Reports screen. Scales down slightly while pressed.
 */
@Composable
private fun StackedReportsPreview(
    latest: ItemReport?,
    totalCount: Int,
    lostCount: Int,
    foundCount: Int,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (interaction.collectIsPressedAsState().value) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "stackPress",
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(20.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
    ) {
        // Peek edges of the pile behind the top card.
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 26.dp),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 13.dp),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)),
            )
        }

        Card(
            onClick = onClick,
            interactionSource = interaction,
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer,
                                ),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    latest?.let {
                        Icon(
                            categoryIcon(it.category),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp),
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tr(R.string.my_reports_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text = totalCount.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                    latest?.let {
                        Text(
                            text = tr(R.string.my_reports_latest, it.title, it.timeAgo),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = tr(R.string.my_reports_breakdown, lostCount, foundCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
