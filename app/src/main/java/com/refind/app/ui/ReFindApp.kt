package com.refind.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.refind.app.R
import com.refind.app.data.Conversation
import com.refind.app.data.DemoData
import com.refind.app.ui.components.LocalAnimateIn
import com.refind.app.ui.screens.ChatScreen
import com.refind.app.ui.screens.FeedScreen
import com.refind.app.ui.screens.ItemDetailScreen
import com.refind.app.ui.screens.LoginScreen
import com.refind.app.ui.screens.MatchFoundScreen
import com.refind.app.ui.screens.MessagesScreen
import com.refind.app.ui.screens.MyReportsScreen
import com.refind.app.ui.screens.ProfileScreen
import com.refind.app.ui.screens.ReportScreen
import com.refind.app.ui.theme.BrandBright
import com.refind.app.ui.theme.BrandDeep
import com.refind.app.ui.theme.BrandPrimary
import com.refind.app.ui.tr
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Every destination of the prototype. An enum so the current screen
 * survives configuration changes (rotation) via rememberSaveable.
 */
enum class Screen {
    Login,
    Feed,
    Messages,
    Profile,
    Report,
    ItemDetail,
    MatchFound,
    Chat,
    MyReports,
}

/** Stack depth drives the direction of the push/pop transitions. */
private fun depthOf(screen: Screen): Int = when (screen) {
    Screen.Login -> 0
    Screen.Feed, Screen.Messages, Screen.Profile -> 1
    Screen.Report, Screen.MyReports -> 2
    Screen.ItemDetail -> 3
    Screen.MatchFound -> 4
    Screen.Chat -> 5
}

/**
 * Root of the prototype: owns the current screen and the demo data, plays
 * the cold-start splash, and animates every navigation with direction-aware
 * transitions. Page pushes run a touch slower so they read on a demo
 * recording; the Report modal keeps its original snappy timing.
 */
@Composable
fun ReFindApp(modifier: Modifier = Modifier) {
    var screen by rememberSaveable { mutableStateOf(Screen.Login) }
    // Cold-start brand moment: plays once per process; rotation and state
    // restoration skip straight to the content.
    var splashDone by rememberSaveable { mutableStateOf(false) }
    var lostReports by remember {
        mutableStateOf(DemoData.lostItems)
    }
    // Only the id is saved: reports are value objects looked up from the lists.
    var detailItemId by rememberSaveable { mutableStateOf<Int?>(null) }
    val detailItem = lostReports.firstOrNull { it.id == detailItemId }
        ?: DemoData.foundItems.firstOrNull { it.id == detailItemId }

    // Navigation intent: MatchFound is a one-shot decision gate — once the
    // user picks a side, backing out should land on where they came from,
    // never on the match screen again.
    var matchOrigin by remember { mutableStateOf(Screen.Feed) }
    var chatOrigin by remember { mutableStateOf(Screen.Feed) }
    var reportOrigin by remember { mutableStateOf(Screen.Feed) }
    // Item detail is reachable from both the feed and My Reports; back
    // returns to wherever the user came from instead of a hard-coded tab.
    var detailOrigin by remember { mutableStateOf(Screen.Feed) }
    var activeConversation by remember {
        mutableStateOf(DemoData.conversations.first())
    }

    // Opens a chat thread from anywhere; the back button returns to `origin`.
    val openChat: (Conversation, Screen) -> Unit = { conv, origin ->
        activeConversation = conv
        chatOrigin = origin
        screen = Screen.Chat
    }

    val matchedLost = remember {
        DemoData.lostItems.first { it.id == DemoData.MATCHED_LOST_ID }
    }
    val matchedFound = remember {
        DemoData.foundItems.first { it.id == DemoData.MATCHED_FOUND_ID }
    }

    // Bottom-bar underline state lives HERE, above AnimatedContent, so the
    // indicator slides continuously between tabs instead of restarting from
    // an initial position every time the bar recomposes.
    val barDensity = LocalDensity.current
    var barWidthPx by remember { mutableStateOf(1f) }
    val horizontalPad = 48.dp
    val underlineWidth = 36.dp
    val selectedTabIdx = when (screen) {
        Screen.Messages -> 1
        Screen.Profile -> 2
        else -> 0
    }
    val slotWidthPx =
        (barWidthPx - with(barDensity) { (horizontalPad * 2).toPx() }) / 3f
    val underlineTargetPx =
        with(barDensity) { horizontalPad.toPx() } +
            slotWidthPx * (selectedTabIdx + 0.5f) -
            with(barDensity) { (underlineWidth / 2).toPx() }
    val underlineLeft = remember { Animatable(underlineTargetPx) }
    LaunchedEffect(underlineTargetPx, barWidthPx) {
        underlineLeft.animateTo(
            underlineTargetPx,
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        )
    }

    val showBottomBar = screen == Screen.Feed ||
        screen == Screen.Messages ||
        screen == Screen.Profile

    // ---- Unified back layer ----
    // One mapping defines the hierarchy (dynamic origins, not fixed parents:
    // the detail page is reachable from the feed AND My Reports). System back
    // and every in-app back arrow go through the same navigateBack path.
    val backTarget: Screen? = when (screen) {
        Screen.Report -> reportOrigin
        Screen.Chat -> chatOrigin
        Screen.MatchFound -> matchOrigin
        Screen.ItemDetail -> detailOrigin
        Screen.MyReports -> Screen.Profile
        else -> null
    }
    val navigateBack: () -> Unit = {
        backTarget?.let { screen = it }
    }
    // Root tabs and Login keep the platform default (back exits the app).
    BackHandler(enabled = backTarget != null) { navigateBack() }

    // ---- Unified entrance-animation layer ----
    // Screens that already played their entrance choreography this session,
    // so returning to a level shows it settled instead of replaying. This is
    // deliberately a plain set (not snapshot state): mutating it must never
    // recompose the outgoing/incoming content mid-transition.
    val playedScreens = remember { mutableSetOf(Screen.Login) }
    LaunchedEffect(screen) { playedScreens.add(screen) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Box(Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = screen,
                    transitionSpec = {
                        val from = depthOf(initialState)
                        val to = depthOf(targetState)
                        when {
                            // Login → Feed: the takeover already floods the screen
                            // with brand colour and the feed header occupies the
                            // same spot — just fade in, no movement at all.
                            initialState == Screen.Login && targetState == Screen.Feed ->
                                fadeIn(tween(300)) togetherWith fadeOut(tween(160))

                            // Report is a modal: it slides up while the page below
                            // dims and shrinks slightly in place (no sideways move).
                            // Modal timing stays at its original snappy values —
                            // only the page transitions are slowed for the demo.
                            targetState == Screen.Report ->
                                (slideInVertically(tween(380)) { it } + fadeIn(tween(380))) togetherWith
                                    (scaleOut(targetScale = 0.96f) + fadeOut(tween(320)))

                            initialState == Screen.Report ->
                                (scaleIn(initialScale = 0.96f) + fadeIn(tween(300))) togetherWith
                                    (slideOutVertically(tween(340)) { it } + fadeOut(tween(340)))

                            // Tab switch: cross-fade.
                            to == from ->
                                fadeIn(tween(360)) togetherWith fadeOut(tween(360))

                            // Push: content slides in from the right.
                            to > from ->
                                (slideInHorizontally(tween(430)) { it / 4 } + fadeIn(tween(430))) togetherWith
                                    (slideOutHorizontally(tween(430)) { -it / 5 } + fadeOut(tween(430)))

                            // Pop: content slides in from the left.
                            else ->
                                (slideInHorizontally(tween(430)) { -it / 4 } + fadeIn(tween(430))) togetherWith
                                    (slideOutHorizontally(tween(430)) { it / 5 } + fadeOut(tween(430)))
                        }
                    },
                    label = "rootNav",
                ) { target ->
                    // Freeze once per content lifetime: the first visit plays
                    // the entrance, every return shows the screen settled.
                    val animateIn = remember(target) { target !in playedScreens }
                    CompositionLocalProvider(LocalAnimateIn provides animateIn) {
                        when (target) {
                            Screen.Login -> LoginScreen(onLogin = { screen = Screen.Feed })

                            Screen.Feed -> FeedScreen(
                                userName = DemoData.USER_NAME,
                                lostItems = lostReports,
                                foundItems = DemoData.foundItems,
                                onNewReport = {
                                    reportOrigin = Screen.Feed
                                    screen = Screen.Report
                                },
                                onItemClick = {
                                    detailItemId = it.id
                                    detailOrigin = Screen.Feed
                                    screen = Screen.ItemDetail
                                },
                            )

                            Screen.Profile -> ProfileScreen(
                                myReports = (lostReports + DemoData.foundItems)
                                    .filter { it.reporter == DemoData.USER_NAME },
                                onOpenReports = { screen = Screen.MyReports },
                                onSignOut = { screen = Screen.Login },
                            )

                            Screen.MyReports -> MyReportsScreen(
                                myReports = (lostReports + DemoData.foundItems)
                                    .filter { it.reporter == DemoData.USER_NAME },
                                onBack = navigateBack,
                                onNewReport = {
                                    reportOrigin = Screen.MyReports
                                    screen = Screen.Report
                                },
                                onItemClick = {
                                    detailItemId = it.id
                                    detailOrigin = Screen.MyReports
                                    screen = Screen.ItemDetail
                                },
                            )

                            Screen.Report -> ReportScreen(
                                onSubmitted = { newItem ->
                                    lostReports = listOf(newItem) + lostReports
                                },
                                onCancel = navigateBack,
                            )

                            Screen.ItemDetail -> detailItem?.let { item ->
                                ItemDetailScreen(
                                    item = item,
                                    hasMatch = item.id == DemoData.MATCHED_LOST_ID,
                                    onBack = navigateBack,
                                    onMatchClick = {
                                        matchOrigin = Screen.ItemDetail
                                        screen = Screen.MatchFound
                                    },
                                    onChatClick = {
                                        chatOrigin = Screen.ItemDetail
                                        screen = Screen.Chat
                                    },
                                )
                            } ?: FeedScreen(
                                userName = DemoData.USER_NAME,
                                lostItems = lostReports,
                                foundItems = DemoData.foundItems,
                                onNewReport = {
                                    reportOrigin = Screen.Feed
                                    screen = Screen.Report
                                },
                                onItemClick = {},
                            )

                            Screen.MatchFound -> MatchFoundScreen(
                                lostItem = matchedLost,
                                foundItem = matchedFound,
                                confidence = DemoData.MATCH_CONFIDENCE,
                                onChat = {
                                    // The gate is passed: back from chat skips MatchFound.
                                    openChat(DemoData.conversations.first(), matchOrigin)
                                },
                                onBack = navigateBack,
                            )

                            // Chats are full-screen: the inbox back arrow returns there,
                            // and no bottom bar competes with the composer for space.
                            Screen.Chat -> ChatScreen(
                                initialMessages = activeConversation.messages,
                                item = activeConversation.item ?: matchedLost,
                                peerName = activeConversation.peerName,
                                peerSubtitle = activeConversation.peerRole,
                                peerOnline = activeConversation.peerOnline,
                                useAppIcon = activeConversation.useAppIcon,
                                onBack = navigateBack,
                            )

                            Screen.Messages -> MessagesScreen(
                                conversations = DemoData.conversations,
                                onOpenChat = { conv -> openChat(conv, Screen.Messages) },
                            )
                        }
                    }
                }
            }

            if (showBottomBar) {
                BottomBar(
                    currentTab = screen,
                    underlineLeftPx = underlineLeft.value,
                    onWidthChange = { barWidthPx = it },
                    onHome = { screen = Screen.Feed },
                    onMessages = { screen = Screen.Messages },
                    onProfile = { screen = Screen.Profile },
                )
            }
        }

        if (!splashDone) {
            SplashOverlay(onDone = { splashDone = true })
        }
    }
}

/**
 * Cold-start brand moment: the app icon pops onto the brand gradient with an
 * expanding halo, the slogan rises in, then the whole layer lifts away into
 * the login screen. Kept slow enough to be read on a demo recording.
 */
@Composable
private fun SplashOverlay(onDone: () -> Unit) {
    val tileScale = remember { Animatable(0f) }
    val halo = remember { Animatable(0f) }
    val wordAlpha = remember { Animatable(0f) }
    val wordRise = remember { Animatable(1f) }
    val sloganAlpha = remember { Animatable(0f) }
    val overlayAlpha = remember { Animatable(1f) }
    val lift = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        launch {
            tileScale.animateTo(
                1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 340f),
            )
        }
        launch {
            delay(160)
            halo.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
        }
        launch {
            delay(380)
            launch { wordAlpha.animateTo(1f, tween(420)) }
            wordRise.animateTo(0f, tween(420, easing = FastOutSlowInEasing))
        }
        launch {
            delay(520)
            sloganAlpha.animateTo(1f, tween(420))
        }
        // Hold the finished composition for a beat, then lift the layer away.
        delay(1650)
        launch { overlayAlpha.animateTo(0f, tween(460, easing = FastOutSlowInEasing)) }
        lift.animateTo(1.07f, tween(460, easing = FastOutSlowInEasing))
        onDone()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = overlayAlpha.value
                scaleX = lift.value
                scaleY = lift.value
            }
            .background(
                Brush.verticalGradient(
                    listOf(BrandDeep, BrandPrimary, BrandBright),
                ),
            ),
    ) {
        // Soft decorative circles keep the brand band from feeling flat.
        Box(
            Modifier
                .align(Alignment.TopStart)
                .offset(x = (-70).dp, y = (-60).dp)
                .size(220.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f)),
        )
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = 90.dp)
                .size(260.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.07f)),
        )
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .graphicsLayer {
                        scaleX = tileScale.value
                        scaleY = tileScale.value
                    },
                contentAlignment = Alignment.Center,
            ) {
                // Expanding halo behind the icon tile.
                Box(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = 0.6f + halo.value * 1.1f
                            scaleY = 0.6f + halo.value * 1.1f
                            alpha = (1f - halo.value) * 0.35f
                        }
                        .clip(CircleShape)
                        .background(Color.White),
                )
                // White icon tile, echoing the on-device launcher look.
                Box(
                    Modifier
                        .fillMaxSize(0.8f)
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = "ReFind",
                        modifier = Modifier.fillMaxSize(0.82f),
                    )
                }
            }
            Spacer(Modifier.height(22.dp))
            Text(
                text = tr(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.graphicsLayer {
                    alpha = wordAlpha.value
                    translationY = 18f * wordRise.value
                },
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = tr(R.string.login_slogan),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.graphicsLayer { alpha = sloganAlpha.value },
            )
        }
    }
}

/** The persistent bottom bar; the underline slides across tabs. */
@Composable
private fun BottomBar(
    currentTab: Screen,
    underlineLeftPx: Float,
    onWidthChange: (Float) -> Unit,
    onHome: () -> Unit,
    onMessages: () -> Unit,
    onProfile: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .onSizeChanged { onWidthChange(it.width.toFloat().coerceAtLeast(1f)) },
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        // Sliding underline, owned by the root so it never resets.
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset {
                    IntOffset(underlineLeftPx.roundToInt(), (-9).dp.roundToPx())
                }
                .size(36.dp, 3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(74.dp)
                .padding(horizontal = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavItem(
                icon = Icons.Outlined.Home,
                activeIcon = Icons.Filled.Home,
                label = tr(R.string.nav_home),
                selected = currentTab == Screen.Feed,
                onClick = onHome,
                modifier = Modifier.weight(1f),
            )
            NavItem(
                icon = Icons.Outlined.Email,
                activeIcon = Icons.Filled.Email,
                label = tr(R.string.nav_chats),
                selected = currentTab == Screen.Messages,
                onClick = onMessages,
                modifier = Modifier.weight(1f),
            )
            NavItem(
                icon = Icons.Outlined.Person,
                activeIcon = Icons.Filled.Person,
                label = tr(R.string.nav_profile),
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
        // Reserved space so labels sit at the same height in all states.
        Spacer(Modifier.height(8.dp))
    }
}
