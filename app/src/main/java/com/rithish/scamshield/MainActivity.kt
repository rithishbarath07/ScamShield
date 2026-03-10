package com.rithish.scamshield

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.rithish.scamshield.service.ScamShieldMonitorService
import com.rithish.scamshield.ui.EnglishStrings
import com.rithish.scamshield.ui.HomeScreen
import com.rithish.scamshield.ui.LocalAppStrings
import com.rithish.scamshield.ui.LocalOnLanguageSwitch
import com.rithish.scamshield.ui.TamilStrings
import com.rithish.scamshield.ui.theme.ScamShieldTheme

class MainActivity : ComponentActivity() {

    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_SMS,
        Manifest.permission.RECEIVE_SMS
    )

    private val scamNumbers = setOf("9095022524")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAndRequestPermissions()

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        // Android 15 blocks SMS_RECEIVED for non-default SMS apps. Use notification access instead.
        promptNotificationAccessIfNeeded()

        // Prompt to disable battery optimization so scam overlay works when app is in background
        promptBatteryOptimizationIfNeeded()

        // Start persistent monitor so protection works when app is swiped away or in background
        startMonitorService()

        val testNumber = "9876543210"

        if (isScamNumber(testNumber)) {
            Log.d("SCAM_TEST", "Scam detected")
        } else {
            Log.d("SCAM_TEST", "Safe number")
        }

        setContent {
            val prefs = getSharedPreferences("app", Context.MODE_PRIVATE)
            var isTamil by remember {
                mutableStateOf(prefs.getBoolean("use_tamil", true))
            }
            val strings = if (isTamil) TamilStrings else EnglishStrings
            ScamShieldTheme {
                CompositionLocalProvider(
                    LocalAppStrings provides strings,
                    LocalOnLanguageSwitch provides {
                        isTamil = !isTamil
                        prefs.edit().putBoolean("use_tamil", isTamil).apply()
                    }
                ) {
                    HomeScreen(
                        strings = strings,
                        isTamil = isTamil,
                        onLanguageSwitch = {
                            isTamil = !isTamil
                            prefs.edit().putBoolean("use_tamil", isTamil).apply()
                        }
                    )
                }
            }
        }
    }

    private fun startMonitorService() {
        val intent = Intent(this, ScamShieldMonitorService::class.java)
        try {
            ContextCompat.startForegroundService(this, intent)
        } catch (_: Exception) { }
    }

    private fun promptBatteryOptimizationIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } catch (_: Exception) {
                // Fallback: open app details so user can disable battery optimization manually
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (_: Exception) { }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val notGranted = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                notGranted.toTypedArray(),
                100
            )
        }
    }

    private fun promptNotificationAccessIfNeeded() {
        val enabled = NotificationManagerCompat.getEnabledListenerPackages(this)
            .contains(packageName)
        if (enabled) return

        try {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        } catch (_: Exception) { }
    }

    private fun isScamNumber(number: String): Boolean {

        Log.d("DEBUG_RAW", "Raw number: $number")

        val cleanedNumber = number
            .replace("+91", "")
            .replace("\\s".toRegex(), "")
            .replace("-", "")
            .takeLast(10)

        Log.d("DEBUG_CLEANED", "Cleaned number: $cleanedNumber")
        Log.d("DEBUG_LIST", "Scam list: $scamNumbers")

        return if (scamNumbers.contains(cleanedNumber)) {
            Log.d("SCAM_ALERT", "Scam call detected!")
            true
        } else {
            Log.d("SCAM_ALERT", "Safe call")
            false
        }
    }
}