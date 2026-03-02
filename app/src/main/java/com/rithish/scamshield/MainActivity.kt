package com.rithish.scamshield

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

        if (!android.provider.Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION
            )
            startActivity(intent)
        }

        val testNumber = "9876543210"

        if (isScamNumber(testNumber)) {
            Log.d("SCAM_TEST", "Scam detected")
        } else {
            Log.d("SCAM_TEST", "Safe number")
        }

        setContent {
            ScamShieldTheme {
                ScamShieldUI()
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

@Composable
fun ScamShieldUI() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Scam Protection Active",
            fontSize = 22.sp
        )
    }
}