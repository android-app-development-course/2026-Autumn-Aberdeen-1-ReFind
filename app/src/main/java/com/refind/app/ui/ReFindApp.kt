package com.refind.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.refind.app.data.DemoData
import com.refind.app.data.ItemReport
import com.refind.app.ui.screens.ChatScreen
import com.refind.app.ui.screens.FeedScreen
import com.refind.app.ui.screens.ItemDetailScreen
import com.refind.app.ui.screens.LoginScreen
import com.refind.app.ui.screens.MatchFoundScreen
import com.refind.app.ui.screens.ProfileScreen
import com.refind.app.ui.screens.ReportScreen

/**
 * Every destination of the prototype. An enum so the current screen
 * survives configuration changes (rotation) via rememberSaveable.
 */
enum class Screen {
    Login,
    Feed,
    Profile,
    Report,
    ItemDetail,
    MatchFound,
    Chat,
}

/** Stack depth drives the direction of the push/pop transitions. */
private fun depthOf(screen: Screen): Int = when (screen) {
    Screen.Login -> 0
    Screen.Feed, Screen.Profile -> 1
    Screen.Report -> 2
    Screen.ItemDetail -> 3
    Screen.MatchFound -> 4
    Screen.Chat -> 5
}

/**
 * Root of the prototype: owns the current screen and the demo data,
 * animates every navigation with direction-aware transitions.
 */
@Composable
fun ReFindApp(modifier: Modifier = Modifier) {
    var screen by rememberSaveable { mutableStateOf(Screen.Login) }
    var lostReports by remember {
        mutableStateOf(DemoData.lostItems)
    }
    // Only the id is saved: reports are value objects looked up from the lists.
    var detailItemId by rememberSaveable { mutableStateOf<Int?>(null) }
    val detailItem = lostReports.firstOrNull { it.id == detailItemId }
        ?: DemoData.foundItems.firstOrNull { it.id == detailItemId }

    val matchedLost = remember {
        DemoData.lostItems.first { it.id == DemoData.MATCHED_LOST_ID }
    }
    val matchedFound = remember {
        DemoData.foundItems.first { it.id == DemoData.MATCHED_FOUND_ID }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AnimatedContent(
            targetState = screen,
            transitionSpec = {
                val from = depthOf(initialState)
                val to = depthOf(targetState)
                when {
                    // Report is a modal: it slides up from the bottom.
                    targetState == Screen.Report ->
                        (slideInVertically(tween(380)) { it } + fadeIn(tween(380))) togetherWith
                            (slideOutHorizontally(tween(380)) { -it / 6 } + fadeOut(tween(380)))

                    initialState == Screen.Report ->
                        (slideInHorizontally(tween(340)) { -it / 6 } + fadeIn(tween(340))) togetherWith
                            (slideOutVertically(tween(340)) { it } + fadeOut(tween(340)))

                    // Tab switch: cross-fade.
                    to == from ->
                        fadeIn(tween(280)) togetherWith fadeOut(tween(280))

                    // Push: content slides in from the right.
                    to > from ->
                        (slideInHorizontally(tween(340)) { it / 4 } + fadeIn(tween(340))) togetherWith
                            (slideOutHorizontally(tween(340)) { -it / 5 } + fadeOut(tween(340)))

                    // Pop: content slides in from the left.
                    else ->
                        (slideInHorizontally(tween(340)) { -it / 4 } + fadeIn(tween(340))) togetherWith
                            (slideOutHorizontally(tween(340)) { it / 5 } + fadeOut(tween(340)))
                }
            },
            label = "rootNav",
        ) { target ->
            when (target) {
                Screen.Login -> LoginScreen(onLogin = { screen = Screen.Feed })

                Screen.Feed -> MainShell(
                    currentTab = target,
                    onHome = { screen = Screen.Feed },
                    onProfile = { screen = Screen.Profile },
                    onReport = { screen = Screen.Report },
                ) {
                    FeedScreen(
                        userName = DemoData.USER_NAME,
                        lostItems = lostReports,
                        foundItems = DemoData.foundItems,
                        matchConfidencePercent = (DemoData.MATCH_CONFIDENCE * 100).toInt(),
                        onMatchClick = { screen = Screen.MatchFound },
                        onItemClick = {
                            detailItemId = it.id
                            screen = Screen.ItemDetail
                        },
                    )
                }

                Screen.Profile -> MainShell(
                    currentTab = target,
                    onHome = { screen = Screen.Feed },
                    onProfile = { screen = Screen.Profile },
                    onReport = { screen = Screen.Report },
                ) {
                    ProfileScreen(
                        myReports = (lostReports + DemoData.foundItems)
                            .filter { it.reporter == DemoData.USER_NAME },
                        onSignOut = { screen = Screen.Login },
                    )
                }

                Screen.Report -> ReportScreen(
                    onSubmitted = { newItem ->
                        lostReports = listOf(newItem) + lostReports
                    },
                    onCancel = { screen = Screen.Feed },
                )

                Screen.ItemDetail -> detailItem?.let { item ->
                    ItemDetailScreen(
                        item = item,
                        hasMatch = item.id == DemoData.MATCHED_LOST_ID,
                        onBack = { screen = Screen.Feed },
                        onMatchClick = { screen = Screen.MatchFound },
                        onChatClick = { screen = Screen.Chat },
                    )
                } ?: FeedScreen(
                    userName = DemoData.USER_NAME,
                    lostItems = lostReports,
                    foundItems = DemoData.foundItems,
                    matchConfidencePercent = (DemoData.MATCH_CONFIDENCE * 100).toInt(),
                    onMatchClick = { screen = Screen.MatchFound },
                    onItemClick = {},
                )

                Screen.MatchFound -> MatchFoundScreen(
                    lostItem = matchedLost,
                    foundItem = matchedFound,
                    confidence = DemoData.MATCH_CONFIDENCE,
                    onChat = { screen = Screen.Chat },
                    onBack = { screen = Screen.Feed },
                )

                Screen.Chat -> ChatScreen(
                    initialMessages = DemoData.chatMessages,
                    onBack = { screen = Screen.MatchFound },
                )
            }
        }
    }
}

/** Feed/Profile shell with the bottom bar and the raised report button. */
@Composable
private fun MainShell(
    currentTab: Screen,
    onHome: () -> Unit,
    onProfile: () -> Unit,
    onReport: () -> Unit,
    content: @Composable () -> Unit,
) {
    // The report button lives above the shell (not inside the bar) so the
    // Surface's clipping can't cut off the half that floats over the content.
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) { content() }
            BottomBar(
                currentTab = currentTab,
                onHome = onHome,
                onProfile = onProfile,
            )
        }
        // Raised report button with a soft white halo, floating over the bar.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-46).dp)
                .shadow(12.dp, CircleShape)
                .size(66.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .padding(5.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary),
                    ),
                )
                .clickable(onClick = onReport),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.Add,
                contentDescription = "New report",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun BottomBar(
    currentTab: Screen,
    onHome: () -> Unit,
    onProfile: () -> Unit,
) {
    // The bar blends into the page: same background colour, hairline on top,
    // selection marked by an animated underline instead of a filled pill.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(74.dp)
                .padding(horizontal = 52.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavItem(
                icon = Icons.Outlined.Home,
                activeIcon = Icons.Filled.Home,
                label = "Home",
                selected = currentTab == Screen.Feed,
                onClick = onHome,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.weight(1f))
            NavItem(
                icon = Icons.Outlined.Person,
                activeIcon = Icons.Filled.Person,
                label = "Profile",
                selected = currentTab == Screen.Profile,
                onClick = onProfile,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    activeIcon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "navTint",
    )
    val underlineAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(220),
        label = "navUnderline",
    )
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            if (selected) activeIcon else icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = tint,
        )
        Spacer(Modifier.height(5.dp))
        // Short animated underline: the selection marker.
        Box(
            modifier = Modifier
                .width(30.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .graphicsLayer { alpha = underlineAlpha }
                .background(MaterialTheme.colorScheme.primary),
        )
    }
}
