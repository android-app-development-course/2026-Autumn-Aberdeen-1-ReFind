package com.refind.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.refind.app.AppInfo
import com.refind.app.R
import com.refind.app.ui.tr
import com.refind.app.ui.components.PasswordField
import kotlinx.coroutines.launch

/**
 * Sign-in screen. A calm brand hero on top, the form floating as a sheet.
 * All demo credentials are accepted: this is a design prototype.
 */
@Composable
fun LoginScreen(
    onLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var username by rememberSaveable { mutableStateOf("Peter") }
    var password by rememberSaveable { mutableStateOf("") }

    val logoScale = remember { Animatable(0f) }
    val heroAlpha = remember { Animatable(0f) }
    val cardOffset = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        launch {
            logoScale.animateTo(
                1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 380f),
            )
        }
        launch { heroAlpha.animateTo(1f, tween(400, delayMillis = 120)) }
        launch {
            cardOffset.animateTo(0f, tween(450, delayMillis = 180, easing = FastOutSlowInEasing))
        }
    }

    // Full-bleed gradient behind everything: the white sheet floats on it.
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.primary, Color(0xFF5C9CF8)),
                ),
            ),
    ) {
        // Brand hero
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(304.dp),
        ) {
            // Language toggle pinned to the top-right corner.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                LanguagePill(onToggle = {
                    val tags = androidx.appcompat.app.AppCompatDelegate
                        .getApplicationLocales().toLanguageTags()
                    val next = if (tags.startsWith("zh")) "en" else "zh"
                    androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                        androidx.core.os.LocaleListCompat.forLanguageTags(next),
                    )
                })
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 48.dp)
                    .graphicsLayer { alpha = heroAlpha.value },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .graphicsLayer {
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                    }
                    .background(Color.White.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp),
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = "ReFind",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = tr(R.string.login_slogan),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
            )
            }
        }

        // Floating form sheet: fills the rest of the screen in white, its
        // rounded top letting the hero gradient peek through the corners.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true)
                .graphicsLayer {
                    translationY = 60f * cardOffset.value
                    alpha = 1f - cardOffset.value * 0.4f
                }
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
        ) {
            Text(
                text = tr(R.string.login_welcome),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = tr(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(tr(R.string.username)) },
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            )
            Spacer(Modifier.height(14.dp))
            PasswordField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = tr(R.string.password),
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text(tr(R.string.sign_in), style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = "ReFind v" + com.refind.app.AppInfo.VERSION,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * Frosted pill showing the active language; tapping toggles the app locale
 * between English and 中文 via per-app locales (instant recreation).
 */
@Composable
private fun LanguagePill(onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val tags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    val label = if (tags.startsWith("zh")) "中文" else "EN"
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.22f),
        modifier = modifier,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}
