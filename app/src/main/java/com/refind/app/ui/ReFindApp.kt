package com.refind.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Surface
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

/** Every destination of the prototype. */
sealed interface Screen {
    data object Login : Screen
    data object Feed : Screen
    data object Profile : Screen
    data object Report : Screen
    data object ItemDetail : Screen
    data object MatchFound : Screen
    data object Chat : Screen
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
    var screen by remember { mutableStateOf<Screen>(Screen.Login) }
    var lostReports by remember {
        mutableStateOf(DemoData.lostItems)
    }
    var detailItem by remember { mutableStateOf<ItemReport?>(null) }

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
                            detailItem = it
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
                    ProfileScreen(myReports = lostReports)
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
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) { content() }
        BottomBar(
            currentTab = currentTab,
            onHome = onHome,
            onProfile = onProfile,
            onReport = onReport,
        )
    }
}

@Composable
private fun BottomBar(
    currentTab: Screen,
    onHome: () -> Unit,
    onProfile: () -> Unit,
    onReport: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 36.dp),
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
            // Raised report button
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-22).dp)
                    .shadow(10.dp, CircleShape)
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
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
    val tint = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            if (selected) activeIcon else icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = tint,
        )
    }
}
