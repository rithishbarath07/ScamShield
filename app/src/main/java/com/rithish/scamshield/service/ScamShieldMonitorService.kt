package com.rithish.scamshield.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.rithish.scamshield.R

/**
 * Persistent foreground service that keeps ScamShield protection active when the app
 * is in background or swiped away. Started once from MainActivity so call/SMS
 * receivers can reliably start the overlay service.
 */
class ScamShieldMonitorService : Service() {

    companion object {
        private const val CHANNEL_ID = "scamshield_monitor"
        private const val CHANNEL_NAME = "ScamShield Protection"
        private const val NOTIFICATION_ID = 1000
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundWithType()
        return START_STICKY
    }

    private fun startForegroundWithType() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW).apply {
                    description = "ScamShield is monitoring calls and SMS"
                    setShowBadge(false)
                }
            )
        }
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ScamShield protection active")
            .setContentText("Monitoring calls and messages")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setSilent(true)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }
}
