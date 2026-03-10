package com.rithish.scamshield.ui

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rithish.scamshield.ui.theme.Amber400
import com.rithish.scamshield.ui.theme.DarkBackground
import com.rithish.scamshield.ui.theme.Emerald400
import com.rithish.scamshield.ui.theme.Slate700
import com.rithish.scamshield.ui.theme.Slate800

@Composable
fun HomeScreen(
    strings: LocaleStrings = LocalAppStrings.current,
    isTamil: Boolean = true,
    onLanguageSwitch: (() -> Unit)? = LocalOnLanguageSwitch.current
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            DarkBackground,
            Slate800,
            DarkBackground
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Language toggle top-right
            if (onLanguageSwitch != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    LanguageChip(
                        labelTa = strings.switchToTamil,
                        labelEn = strings.switchToEnglish,
                        isTamil = isTamil,
                        onClick = onLanguageSwitch
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.weight(0.2f))

            // Shield icon (emoji) with glow
            val scale = animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(800),
                label = "shieldScale"
            )
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale.value)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Emerald400.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🛡️",
                    fontSize = 56.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App name
            Text(
                text = strings.appName,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Status card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Emerald400.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = strings.protectionActive,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Emerald400,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = strings.protectionActiveSubtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Status pill
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Amber400.copy(alpha = 0.2f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Amber400)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = strings.statusMonitoring,
                        style = MaterialTheme.typography.labelLarge,
                        color = Amber400
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.4f))
        }
    }
}

@Composable
private fun LanguageChip(
    labelTa: String,
    labelEn: String,
    isTamil: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isTamil) labelEn else labelTa,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = "⇄",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
