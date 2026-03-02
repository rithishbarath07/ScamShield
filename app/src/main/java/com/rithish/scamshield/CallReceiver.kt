package com.rithish.scamshield

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log

class CallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {

            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

            if (state == TelephonyManager.EXTRA_STATE_RINGING) {

                val incomingNumber =
                    intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

                Log.d("CALL_DETECTED", "Incoming call from: $incomingNumber")

                incomingNumber?.let { number ->

                    Log.d("DEBUG_RAW", "Raw number: $number")

                    val cleanedNumber = number
                        .replace("+91", "")
                        .replace("\\s".toRegex(), "")
                        .replace("-", "")
                        .takeLast(10)

                    Log.d("DEBUG_CLEANED", "Cleaned number: $cleanedNumber")

                    val scamNumbers = setOf("9095022524")

                    if (scamNumbers.contains(cleanedNumber)) {

                        Log.d("SCAM_ALERT", "Scam call detected!")

                        val serviceIntent = Intent(context, ScamOverlayService::class.java)
                        context.startService(serviceIntent)

                        if (state == TelephonyManager.EXTRA_STATE_IDLE) {
                            val stopIntent = Intent(context, ScamOverlayService::class.java)
                            context.stopService(stopIntent)
                        }

                    } else {
                        Log.d("SCAM_ALERT", "Safe call")
                    }
                }
            }
        }
    }
}