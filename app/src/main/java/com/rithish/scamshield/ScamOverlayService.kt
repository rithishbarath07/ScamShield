package com.rithish.scamshield

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color

class ScamOverlayService : Service() {

    private var overlayView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        layoutParams.gravity = Gravity.CENTER

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.RED)
            gravity = Gravity.CENTER
        }

        val warningText = TextView(this).apply {
            text = "⚠ POSSIBLE SCAM CALL ⚠\n\nDo NOT share OTP or bank details."
            textSize = 22f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        val closeButton = Button(this).apply {
            text = "CLOSE"
            setOnClickListener {
                stopSelf()
            }
        }

        layout.addView(warningText)
        layout.addView(closeButton)

        overlayView = layout

        windowManager.addView(overlayView, layoutParams)
    }

    override fun onDestroy() {
        super.onDestroy()
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView?.let { windowManager.removeView(it) }
        overlayView = null
    }
}