package com.rithish.scamshield.ui

import androidx.compose.runtime.compositionLocalOf

val LocalAppStrings = compositionLocalOf { TamilStrings }
val LocalOnLanguageSwitch = compositionLocalOf<(() -> Unit)?> { null }
