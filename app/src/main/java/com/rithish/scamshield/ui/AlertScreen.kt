package com.rithish.scamshield.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Reserved for future in-app alerts.
 * Current overlay alerts are implemented via WindowManager in ScamOverlayService.
 */
@Composable
fun AlertScreen(
    title: String,
    message: String,
    onPrimaryAction: (() -> Unit)? = null,
    primaryActionLabel: String = "OK"
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Red),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = message,
            color = Color.White,
            fontSize = 20.sp
        )

        if (onPrimaryAction != null) {
            Button(onClick = onPrimaryAction) {
                Text(text = primaryActionLabel, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

