package com.refind.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.refind.app.data.Category
import com.refind.app.data.DemoData
import com.refind.app.data.ItemReport
import com.refind.app.data.ItemStatus
import com.refind.app.ui.components.categoryIcon
import com.refind.app.ui.components.pressScale

private val COLOURS = listOf("Blue", "Black", "White", "Silver", "Orange", "Other")
private val TIMES = listOf("Just now", "1h ago", "Yesterday", "2 days ago")
private val REWARDS = listOf(null, 5, 20)

/**
 * Three-step report wizard (type -> item -> where&when) followed by a
 * success page. Steps slide horizontally with the progress bar growing.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportScreen(
    onSubmitted: (ItemReport) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var status by rememberSaveable { mutableStateOf(ItemStatus.LOST) }
    var category by remember { mutableStateOf<Category?>(null) }
    var title by rememberSaveable { mutableStateOf("") }
    var colour by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("Main Library, 2F") }
    var timeAgo by rememberSaveable { mutableStateOf("Just now") }
    var reward by remember { mutableStateOf<Int?>(null) }

    val progress by animateFloatAsState(
        targetValue = (step + 1) / 4f,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "reportProgress",
    )

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onCancel) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "Cancel",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = "New report",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = if (step < 3) "Step ${step + 1} of 3" else "Done",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(12.dp))
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        AnimatedContent(
            targetState = step,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                val forward = targetState > initialState
                val enter = slideInHorizontally(tween(300)) { full ->
                    if (forward) full / 3 else -full / 3
                } + fadeIn(tween(300))
                val exit = slideOutHorizontally(tween(300)) { full ->
                    if (forward) -full / 3 else full / 3
                } + fadeOut(tween(300))
                enter togetherWith exit
            },
            label = "reportStep",
        ) { currentStep ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp),
            ) {
                when (currentStep) {
                    0 -> TypeStep(
                        selected = status,
                        onSelect = { status = it },
                    )

                    1 -> ItemStep(
                        category = category,
                        onCategory = { category = it },
                        title = title,
                        onTitle = { title = it },
                        colour = colour,
                        onColour = { colour = it },
                    )

                    2 -> WhereWhenStep(
                        location = location,
                        onLocation = { location = it },
                        timeAgo = timeAgo,
                        onTime = { timeAgo = it },
                        reward = reward,
                        onReward = { reward = it },
                    )

                    else -> SuccessStep()
                }
            }
        }

        // Footer action
        Box(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            when (step) {
                0 -> NextButton(
                    text = "Continue",
                    enabled = true,
                ) { step = 1 }
                1 -> NextButton(
                    text = "Continue",
                    enabled = category != null && title.isNotBlank(),
                ) { step = 2 }
                2 -> NextButton(
                    text = "Submit report",
                    enabled = true,
                ) {
                    step = 3
                    if (status == ItemStatus.LOST) {
                        onSubmitted(
                            ItemReport(
                                id = -(System.currentTimeMillis() % 100_000).toInt(),
                                title = title.ifBlank { "Untitled item" },
                                category = category ?: Category.OTHER,
                                location = location,
                                timeAgo = timeAgo,
                                status = status,
                                colour = colour,
                                reward = reward,
                                reporter = DemoData.USER_NAME,
                            ),
                        )
                    }
                }
                else -> NextButton(text = "Back to feed", enabled = true) { onCancel() }
            }
        }
    }
}

@Composable
private fun NextButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(18.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun TypeStep(
    selected: ItemStatus,
    onSelect: (ItemStatus) -> Unit,
) {
    Text(
        text = "What happened?",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(Modifier.height(6.dp))
    Text(
        text = "Pick the kind of report you are about to create.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(20.dp))

    listOf(
        Triple(
            ItemStatus.LOST,
            "I lost something",
            "Tell ReFind what's missing — we'll watch every new found report for you.",
        ),
        Triple(
            ItemStatus.FOUND,
            "I found something",
            "Post it so the owner can claim it through a private verification question.",
        ),
    ).forEach { (status, heading, description) ->
        val isSelected = selected == status
        val interaction = remember { MutableInteractionSource() }
        val scale by androidx.compose.animation.core.animateFloatAsState(
            targetValue = if (isSelected) 1.02f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "typeScale",
        )
        Surface(
            onClick = { onSelect(status) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .pressScale(interaction, pressedScale = 0.98f),
            interactionSource = interaction,
            shape = RoundedCornerShape(20.dp),
            color = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
            border = if (isSelected) {
                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            } else {
                BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            },
        ) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (status == ItemStatus.LOST) Icons.Outlined.SearchOff else Icons.Outlined.Search,
                    contentDescription = null,
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(34.dp),
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = heading,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ItemStep(
    category: Category?,
    onCategory: (Category) -> Unit,
    title: String,
    onTitle: (String) -> Unit,
    colour: String,
    onColour: (String) -> Unit,
) {
    Text(
        text = "What is it?",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(Modifier.height(16.dp))
    Text(
        text = "CATEGORY",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(10.dp))
    val grid = Category.entries
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        grid.chunked(4).forEach { rowCategories ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowCategories.forEach { cat ->
                    val isSelected = category == cat
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                            )
                            .clickable { onCategory(cat) }
                            .padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            categoryIcon(cat),
                            contentDescription = cat.label,
                            tint = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = cat.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }
        }
    }

    Spacer(Modifier.height(18.dp))
    OutlinedTextField(
        value = title,
        onValueChange = onTitle,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Item name") },
        placeholder = { Text("e.g. Blue water bottle") },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    )

    Spacer(Modifier.height(18.dp))
    Text(
        text = "COLOUR",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(10.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        COLOURS.forEach { c ->
            val isSelected = colour == c
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    )
                    .clickable { onColour(c) }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
            ) {
                Text(
                    text = c,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WhereWhenStep(
    location: String,
    onLocation: (String) -> Unit,
    timeAgo: String,
    onTime: (String) -> Unit,
    reward: Int?,
    onReward: (Int?) -> Unit,
) {
    Text(
        text = "Where & when?",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(Modifier.height(16.dp))
    Text(
        text = "LAST SEEN AT",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(10.dp))
    OutlinedTextField(
        value = location,
        onValueChange = onLocation,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    )
    Spacer(Modifier.height(18.dp))
    Text(
        text = "WHEN",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(10.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TIMES.forEach { t ->
            val isSelected = timeAgo == t
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    )
                    .clickable { onTime(t) }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
            ) {
                Text(
                    text = t,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
    Spacer(Modifier.height(18.dp))
    Text(
        text = "REWARD (OPTIONAL)",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        REWARDS.forEach { r ->
            val label = r?.let { "$it pts" } ?: "None"
            val isSelected = reward == r
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    )
                    .clickable { onReward(r) }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

/** The check-mark reveal after submitting. */
@Composable
private fun SuccessStep() {
    val circleScale = remember { androidx.compose.animation.core.Animatable(0f) }
    val checkAlpha = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(Unit) {
        circleScale.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 300f),
        )
        checkAlpha.animateTo(1f, tween(220, delayMillis = 60))
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(60.dp))
        Box(
            modifier = Modifier
                .size(104.dp)
                .graphicsLayer {
                    scaleX = circleScale.value
                    scaleY = circleScale.value
                }
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(52.dp)
                    .graphicsLayer { alpha = checkAlpha.value },
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Report submitted!",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Nearby finders have been notified. We'll alert you the moment a match appears.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
