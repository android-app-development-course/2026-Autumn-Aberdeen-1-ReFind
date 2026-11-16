package com.refind.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.refind.app.R
import com.refind.app.ui.tr
import com.refind.app.data.ItemReport
import com.refind.app.ui.components.StatusBadge
import com.refind.app.ui.components.categoryIcon
import com.refind.app.ui.theme.LostOrangeContainer
import com.refind.app.ui.theme.MatchGreen
import com.refind.app.ui.theme.MatchGreenContainer
import com.refind.app.ui.theme.Blue100
import com.refind.app.ui.theme.Blue600
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val MATCH_FACTORS = listOf(
    R.string.factor_category,
    R.string.factor_colour,
    R.string.factor_location,
    R.string.factor_time,
)

/**
 * The signature moment: two reports fly in from the sides, link together,
 * a confidence ring sweeps to the score, factor chips pop in one by one.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MatchFoundScreen(
    lostItem: ItemReport,
    foundItem: ItemReport,
    confidence: Float,
    onChat: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val slideIn = remember { Animatable(0f) }
    val ring = remember { Animatable(0f) }
    val titleScale = remember { Animatable(0f) }
    val actionsAlpha = remember { Animatable(0f) }
    var chipsShown by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        launch {
            slideIn.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        }
        delay(520)
        launch {
            ring.animateTo(confidence, tween(950, easing = FastOutSlowInEasing))
        }
        launch {
            titleScale.animateTo(
                1f,
                spring(dampingRatio = 0.55f, stiffness = 320f),
            )
        }
        delay(300)
        for (i in 1..MATCH_FACTORS.size) {
            chipsShown = i
            delay(170)
        }
        delay(80)
        actionsAlpha.animateTo(1f, tween(280))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        // Headline
        Text(
            text = tr(R.string.its_a_match),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer {
                scaleX = titleScale.value
                scaleY = titleScale.value
                alpha = (titleScale.value * 2).coerceAtMost(1f)
            },
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = tr(R.string.paired_automatically),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(26.dp))

        // The two reports flying together
        val pxPerDp = with(density) { 1.dp.toPx() }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MatchSideCard(
                item = lostItem,
                caption = tr(R.string.match_your_report),
                isLost = true,
                offsetX = -(1f - slideIn.value) * 240f * pxPerDp,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(10.dp))
            // Linking badge appears once the cards meet
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .graphicsLayer {
                        val t = ((slideIn.value - 0.85f) / 0.15f).coerceIn(0f, 1f)
                        scaleX = t
                        scaleY = t
                        alpha = t
                    }
                    .background(MatchGreen, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(10.dp))
            MatchSideCard(
                item = foundItem,
                caption = tr(R.string.match_finder_find, foundItem.reporter),
                isLost = false,
                offsetX = (1f - slideIn.value) * 240f * pxPerDp,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(30.dp))

        // Confidence ring
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(170.dp)) {
                val stroke = 14.dp.toPx()
                drawArc(
                    color = Blue100,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
                drawArc(
                    color = Blue600,
                    startAngle = -90f,
                    sweepAngle = 360f * ring.value,
                    useCenter = false,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${(ring.value * 100).roundToInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = tr(R.string.confidence_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        // Matching factor chips, popping in sequence
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            MATCH_FACTORS.forEachIndexed { index, factorRes ->
                if (index < chipsShown) {
                    FactorChip(
                        label = tr(factorRes),
                        scaleIn = true,
                        modifier = Modifier,
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = tr(R.string.matched_by_note),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(26.dp))

        Column(
            modifier = Modifier.graphicsLayer { alpha = actionsAlpha.value },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = onChat,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp),
            ) {
                Icon(
                    Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(tr(R.string.start_verification_chat), style = MaterialTheme.typography.labelLarge)
            }
            TextButton(onClick = onBack) {
                Text(
                    tr(R.string.not_my_item),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun MatchSideCard(
    item: ItemReport,
    caption: String,
    isLost: Boolean,
    offsetX: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.graphicsLayer { translationX = offsetX },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (isLost) LostOrangeContainer else Blue100),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                categoryIcon(item.category),
                contentDescription = item.category.label,
                tint = if (isLost) com.refind.app.ui.theme.LostOrange else Blue600,
                modifier = Modifier.size(36.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        Text(
            text = caption,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FactorChip(
    label: String,
    scaleIn: Boolean,
    modifier: Modifier = Modifier,
) {
    val appear = remember { Animatable(if (scaleIn) 0f else 1f) }
    LaunchedEffect(Unit) {
        appear.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 500f),
        )
    }
    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = appear.value
                scaleY = appear.value
                alpha = (appear.value * 2).coerceAtMost(1f)
            }
            .clip(RoundedCornerShape(12.dp))
            .background(MatchGreenContainer)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Outlined.Check,
            contentDescription = null,
            tint = MatchGreen,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
