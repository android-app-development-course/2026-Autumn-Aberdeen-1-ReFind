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
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.refind.app.data.Category
import com.refind.app.data.DemoData
import com.refind.app.data.ItemReport
import com.refind.app.R
import com.refind.app.ui.tr
import com.refind.app.data.ItemStatus
import com.refind.app.ui.components.ItemPhotoPicker
import com.refind.app.ui.components.categoryIcon
import com.refind.app.ui.components.pressScale

/** Name -> real swatch colour, so chips can show what they stand for. */
private val COLOURS = listOf(
    "Blue" to androidx.compose.ui.graphics.Color(0xFF2196F3),
    "Black" to androidx.compose.ui.graphics.Color(0xFF212121),
    "White" to androidx.compose.ui.graphics.Color(0xFFF5F5F5),
    "Silver" to androidx.compose.ui.graphics.Color(0xFFC8C8C8),
    "Orange" to androidx.compose.ui.graphics.Color(0xFFFF9800),
    "Green" to androidx.compose.ui.graphics.Color(0xFF4CAF50),
    "Red" to androidx.compose.ui.graphics.Color(0xFFF44336),
    "Purple" to androidx.compose.ui.graphics.Color(0xFF9C27B0),
    "Pink" to androidx.compose.ui.graphics.Color(0xFFEC407A),
    "Yellow" to androidx.compose.ui.graphics.Color(0xFFFDD835),
)
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
    var photo by rememberSaveable { mutableStateOf<String?>(null) }
    var location by rememberSaveable { mutableStateOf("Main Library, 2F") }
    var timeAgo by rememberSaveable { mutableStateOf("Just now") }
    var customTimeText by rememberSaveable { mutableStateOf("") }
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
                .statusBarsPadding()
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
                text = tr(R.string.new_report),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = if (step < 3) tr(R.string.step_of, step + 1, 3) else tr(R.string.done),
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
                        photoUri = photo,
                        onPhoto = { photo = it },
                    )

                    2 -> WhereWhenStep(
                        location = location,
                        onLocation = { location = it },
                        timeAgo = timeAgo,
                        onTime = { timeAgo = it },
                        customTimeText = customTimeText,
                        onCustomTime = {
                            customTimeText = it
                            timeAgo = it
                        },
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
                    text = tr(R.string.action_continue),
                    enabled = true,
                ) { step = 1 }
                1 -> NextButton(
                    text = tr(R.string.action_continue),
                    enabled = category != null && title.isNotBlank(),
                ) { step = 2 }
                2 -> NextButton(
                    text = tr(R.string.submit_report),
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
                                photoUri = photo,
                            ),
                        )
                    }
                }
                else -> NextButton(text = tr(R.string.back_to_feed), enabled = true) { onCancel() }
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
        text = tr(R.string.what_happened),
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(Modifier.height(6.dp))
    Text(
        text = tr(R.string.pick_kind),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(20.dp))

    listOf(
        Triple(
            ItemStatus.LOST,
            tr(R.string.lost_heading),
            tr(R.string.lost_description),
        ),
        Triple(
            ItemStatus.FOUND,
            tr(R.string.found_heading),
            tr(R.string.found_description),
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
    photoUri: String?,
    onPhoto: (String?) -> Unit,
) {
    Text(
        text = tr(R.string.what_is_it),
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(Modifier.height(16.dp))
    Text(
        text = tr(R.string.label_category),
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
        label = { Text(tr(R.string.item_name)) },
        placeholder = { Text(tr(R.string.item_name_hint)) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    )

    Spacer(Modifier.height(18.dp))
    Text(
        text = tr(R.string.label_item_photo),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(10.dp))
    ItemPhotoPicker(
        photoUri = photoUri,
        onPhotoChanged = onPhoto,
    )

    Spacer(Modifier.height(18.dp))
    Text(
        text = tr(R.string.label_colour),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(10.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        COLOURS.forEach { (name, swatch) ->
            val isSelected = colour == name
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    )
                    .clickable { onColour(name) }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(swatch)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = androidx.compose.foundation.shape.CircleShape,
                        ),
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    text = name,
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

@OptIn(ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun WhereWhenStep(
    location: String,
    onLocation: (String) -> Unit,
    timeAgo: String,
    onTime: (String) -> Unit,
    customTimeText: String,
    onCustomTime: (String) -> Unit,
    reward: Int?,
    onReward: (Int?) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pickedDateMillis by remember { mutableStateOf<Long?>(null) }
    val customSelected = customTimeText.isNotBlank()
    Text(
        text = tr(R.string.label_where_when),
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(Modifier.height(16.dp))
    Text(
        text = tr(R.string.label_last_seen),
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
        text = tr(R.string.label_when),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(10.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TIMES.forEach { t ->
            val isSelected = timeAgo == t && !customSelected
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
                    .clickable {
                        onTime(t)
                        onCustomTime("")
                    }
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
        // Custom: opens the date + time pickers below.
        val isCustom = customSelected
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isCustom) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                )
                .clickable { showDatePicker = true }
                .padding(horizontal = 14.dp, vertical = 9.dp),
        ) {
            Text(
                text = tr(R.string.custom),
                style = MaterialTheme.typography.labelLarge,
                color = if (isCustom) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
    if (customSelected) {
        Text(
            text = tr(R.string.custom_time_prefix, customTimeText),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp),
        )
    }

    // Date picker, then time picker, in sequence.
    if (showDatePicker) {
        val datePickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis(),
        )
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickedDateMillis = datePickerState.selectedDateMillis
                            ?: System.currentTimeMillis()
                        showDatePicker = false
                        showTimePicker = true
                    },
                ) { Text(tr(R.string.action_next)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(tr(R.string.action_cancel)) }
            },
        ) {
            androidx.compose.material3.DatePicker(state = datePickerState)
        }
    }
    if (showTimePicker) {
        val timeState = androidx.compose.material3.rememberTimePickerState(is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(tr(R.string.pick_a_time)) },
            text = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.TimePicker(state = timeState)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val calendar = java.util.Calendar.getInstance().apply {
                            timeInMillis = pickedDateMillis ?: System.currentTimeMillis()
                            set(java.util.Calendar.HOUR_OF_DAY, timeState.hour)
                            set(java.util.Calendar.MINUTE, timeState.minute)
                        }
                        val text = java.text.SimpleDateFormat(
                            "d MMM, HH:mm",
                            java.util.Locale.ENGLISH,
                        ).format(calendar.time)
                        onCustomTime(text)
                        showTimePicker = false
                    },
                ) { Text(tr(R.string.action_set)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text(tr(R.string.action_cancel)) }
            },
        )
    }
    Spacer(Modifier.height(18.dp))
    Text(
        text = tr(R.string.label_reward),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(10.dp))
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        REWARDS.forEach { r ->
            val label = r?.let { tr(R.string.reward_points, it) } ?: tr(R.string.reward_none)
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
            text = tr(R.string.report_submitted),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = tr(R.string.report_success_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
