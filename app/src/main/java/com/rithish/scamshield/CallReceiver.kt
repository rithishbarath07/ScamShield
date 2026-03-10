package com.rithish.scamshield.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.rithish.scamshield.logic.CallStateTracker
import com.rithish.scamshield.logic.ScamDetector
import com.rithish.scamshield.service.ScamOverlayService

class CallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {

            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            Log.d("CALL_STATE", "Phone state changed: $state")

            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    val incomingNumber =
                        intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

                    Log.d("CALL_DETECTED", "Incoming call from: $incomingNumber")

                    val isScam = ScamDetector.isScamNumber(incomingNumber)
                    val normalized = ScamDetector.normalizeNumber(incomingNumber)

                    Log.d("SCAM_CHECK", "Normalized: $normalized, isScam: $isScam")

                    CallStateTracker.onIncomingCallRinging(normalized, isScam)

                    if (isScam) {
                        Log.d("SCAM_ALERT", "Scam call detected!")

                        val serviceIntent = Intent(context, ScamOverlayService::class.java).apply {
                            putExtra(ScamOverlayService.EXTRA_PHONE_NUMBER, normalized)
                            putExtra(
                                ScamOverlayService.EXTRA_ALERT_TYPE,
                                ScamOverlayService.ALERT_TYPE_SCAM_CALL
                            )
                        }

                        // Use foreground service for reliability on modern Android versions
                        ContextCompat.startForegroundService(context, serviceIntent)
                    } else {
                        Log.d("SCAM_ALERT", "Safe or unknown call")
                    }
                }

                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    Log.d("CALL_STATE", "Call is active (OFFHOOK)")
                    CallStateTracker.onCallOffhook()
                }

                TelephonyManager.EXTRA_STATE_IDLE -> {
                    Log.d("CALL_STATE", "Call ended (IDLE) – stopping overlay if running")
                    CallStateTracker.onCallIdle()
                    val stopIntent = Intent(context, ScamOverlayService::class.java)
                    context.stopService(stopIntent)
                }
            }
        }
    }
}