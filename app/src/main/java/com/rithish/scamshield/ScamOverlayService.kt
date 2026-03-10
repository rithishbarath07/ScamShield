package com.rithish.scamshield.service

import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.rithish.scamshield.R
import com.rithish.scamshield.overlay.OverlayController
import com.rithish.scamshield.voice.VoiceWarningManager

class ScamOverlayService : Service() {

    companion object {
        const val EXTRA_PHONE_NUMBER: String = "extra_phone_number"
        const val EXTRA_ALERT_TYPE: String = "extra_alert_type"

        const val ALERT_TYPE_SCAM_CALL: String = "alert_scam_call"
        const val ALERT_TYPE_OTP_ONLY: String = "alert_otp_only"
        const val ALERT_TYPE_HIGH_RISK: String = "alert_high_risk"

        private const val NOTIFICATION_CHANNEL_ID = "scamshield_calls"
        private const val NOTIFICATION_CHANNEL_NAME = "ScamShield Call Protection"
        private const val NOTIFICATION_ID = 1001
        private const val TAG = "ScamOverlayService"
    }

    private var currentAlertType: String = ALERT_TYPE_SCAM_CALL
    private lateinit var overlayController: OverlayController

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        overlayController = OverlayController(
            context = this,
            allowEndCall = true,
            onDismiss = { stopSelf() }
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        currentAlertType =
            intent?.getStringExtra(EXTRA_ALERT_TYPE) ?: ALERT_TYPE_SCAM_CALL

        if (!startAsForegroundService()) {
            showFallbackNotification(currentAlertType)
            stopSelf()
            return START_NOT_STICKY
        }

        overlayController.show(currentAlertType)
        overlayController.update(currentAlertType)

        when (currentAlertType) {
            ALERT_TYPE_SCAM_CALL -> VoiceWarningManager.speakSuspectedCallWarning(this)
            ALERT_TYPE_HIGH_RISK -> VoiceWarningManager.speakHighRiskWarning(this)
            ALERT_TYPE_OTP_ONLY -> VoiceWarningManager.speakOtpWarning(this)
            else -> VoiceWarningManager.speakOtpWarning(this)
        }

        return START_STICKY
    }

    /**
     * Promotes service to foreground. Returns false if foreground start is not allowed
     * (e.g. app in background without battery optimization disabled).
     */
    private fun startAsForegroundService(): Boolean {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Alerts while monitoring for scam calls"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("ScamShield Protection Active")
            .setContentText("Monitoring current call for possible scams.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()

        val fgsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(NOTIFICATION_ID, notification, fgsType)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            true
        } catch (e: ForegroundServiceStartNotAllowedException) {
            Log.w(TAG, "Cannot start foreground service from background.", e)
            false
        }
    }

    private fun showFallbackNotification(alertType: String = ALERT_TYPE_SCAM_CALL) {
        val (title, text) = when (alertType) {
            ALERT_TYPE_OTP_ONLY -> "⚠ OTP Received" to "Do NOT share this code with anyone. Open ScamShield for full protection."
            ALERT_TYPE_HIGH_RISK -> "🚨 Possible Fraud Attempt" to "End the call immediately. Do NOT share OTP or bank details."
            else -> "⚠ Possible Scam Call" to "Do NOT share OTP or bank details. Open ScamShield and disable battery optimization for reliable protection."
        }
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayController.dismiss()
    }
}
