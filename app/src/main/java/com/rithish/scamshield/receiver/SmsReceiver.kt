package com.rithish.scamshield.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import androidx.core.content.ContextCompat
import com.rithish.scamshield.logic.CallStateTracker
import com.rithish.scamshield.service.ScamOverlayService

/**
 * Receives incoming SMS and detects OTP messages.
 */
class SmsReceiver : BroadcastReceiver() {

    private val otpCodeRegex = Regex("\\b\\d{4,8}\\b")
    private val otpKeywordRegex = Regex(
        "\\b(otp|verification|one time password|one time code|verification code|your code|passcode|pin code)\\b",
        RegexOption.IGNORE_CASE
    )
    private val otpWordFlexibleRegex =
        Regex("o\\s*\\.?\\s*t\\s*\\.?\\s*p", RegexOption.IGNORE_CASE)

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("SMS_RECEIVER", "onReceive called, action=${intent.action}")
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            Log.d("SMS_RECEIVER", "Ignoring non-SMS action")
            return
        }

        val messages = try {
            Telephony.Sms.Intents.getMessagesFromIntent(intent).toList()
        } catch (e: Exception) {
            Log.e("SMS_RECEIVER", "Failed to extract SMS messages from intent", e)
            emptyList()
        }
        if (messages.isEmpty()) {
            Log.w("SMS_RECEIVER", "No SMS messages found in intent extras")
            return
        }

        val fullBody = messages.joinToString(separator = " ") { it.messageBody.orEmpty() }
        Log.d("SMS_RECEIVED", "Message body: $fullBody")

        val hasOtpCode = otpCodeRegex.containsMatchIn(fullBody)
        val hasOtpKeyword = otpKeywordRegex.containsMatchIn(fullBody)
        val hasDigits = fullBody.any { it.isDigit() }
        val hasOtpWord = otpWordFlexibleRegex.containsMatchIn(fullBody) ||
            fullBody.contains("otp", ignoreCase = true)

        // Treat as OTP if:
        // - we clearly see "otp" in any format, OR
        // - we see a 4–8 digit code, OR
        // - we see OTP-related keywords + any digits.
        val isOtpLike = hasOtpWord || hasOtpCode || (hasOtpKeyword && hasDigits)

        if (!isOtpLike) {
            Log.d("SMS_OTP_CHECK", "Not treated as OTP message.")
            return
        }

        Log.d("SMS_OTP_CHECK", "OTP‑like message detected.")

        // Decide alert type based on current call state
        val alertType =
            if (CallStateTracker.isCallActive) {
                ScamOverlayService.ALERT_TYPE_HIGH_RISK
            } else {
                ScamOverlayService.ALERT_TYPE_OTP_ONLY
            }

        val overlayIntent = Intent(context, ScamOverlayService::class.java).apply {
            putExtra(ScamOverlayService.EXTRA_ALERT_TYPE, alertType)
        }

        try {
            ContextCompat.startForegroundService(context, overlayIntent)
            Log.d("SMS_OTP_CHECK", "Started ScamOverlayService for OTP alert. alertType=$alertType")
        } catch (e: Exception) {
            Log.e("SMS_OTP_CHECK", "Failed to start ScamOverlayService from SMS", e)
        }
    }
}
