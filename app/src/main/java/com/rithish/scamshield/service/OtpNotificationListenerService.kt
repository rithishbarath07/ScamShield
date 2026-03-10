package com.rithish.scamshield.service

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.rithish.scamshield.logic.CallStateTracker
import com.rithish.scamshield.overlay.OverlayController
import com.rithish.scamshield.voice.VoiceWarningManager

class OtpNotificationListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "OtpNotifListener"
    }

    private val otpCodeRegex = Regex("\\b\\d{4,8}\\b")
    private val otpKeywordRegex = Regex(
        "\\b(otp|verification|one time password|one time code|verification code|your code|passcode|pin code)\\b",
        RegexOption.IGNORE_CASE
    )
    private val otpWordFlexibleRegex =
        Regex("o\\s*\\.?\\s*t\\s*\\.?\\s*p", RegexOption.IGNORE_CASE)

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate called")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification listener connected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        if (sbn.packageName == packageName) return

        val n = sbn.notification ?: return
        val extras = n.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString().orEmpty()

        val full = listOf(title, text, bigText, subText)
            .filter { it.isNotBlank() }
            .joinToString(separator = " ")
            .trim()

        if (full.isBlank()) return

        val hasOtpKeyword = otpKeywordRegex.containsMatchIn(full)
        val hasOtpWord = otpWordFlexibleRegex.containsMatchIn(full) ||
            full.contains("otp", ignoreCase = true)
        val hasOtpCode = otpCodeRegex.containsMatchIn(full)
        val hasDigits = full.any { it.isDigit() }

        // Treat as OTP if:
        // - we clearly see "otp" in any format, OR
        // - we see a 4–8 digit code, OR
        // - we see OTP-related keywords + any digits.
        val isOtpLike = hasOtpWord || hasOtpCode || (hasOtpKeyword && hasDigits)
        if (!isOtpLike) {
            Log.d(TAG, "Notification ignored (not OTP-like) from ${sbn.packageName}")
            return
        }

        val alertType =
            if (CallStateTracker.isCallActive) {
                ScamOverlayService.ALERT_TYPE_HIGH_RISK
            } else {
                ScamOverlayService.ALERT_TYPE_OTP_ONLY
            }

        Log.d(TAG, "OTP-like notification from ${sbn.packageName}: $full, alertType=$alertType")

        val overlayIntent = Intent(this, ScamOverlayService::class.java).apply {
            putExtra(ScamOverlayService.EXTRA_ALERT_TYPE, alertType)
        }

        try {
            androidx.core.content.ContextCompat.startForegroundService(this, overlayIntent)
            Log.d(TAG, "Requested ScamOverlayService start from notification listener")
            when (alertType) {
                ScamOverlayService.ALERT_TYPE_HIGH_RISK -> VoiceWarningManager.speakHighRiskWarning(this)
                else -> VoiceWarningManager.speakOtpWarning(this)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start ScamOverlayService from notification listener", e)
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy called")
        super.onDestroy()
    }
}

