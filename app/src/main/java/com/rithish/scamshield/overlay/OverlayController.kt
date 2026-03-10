package com.rithish.scamshield.overlay

import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.TypedValue
import android.telecom.TelecomManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.rithish.scamshield.service.ScamOverlayService

class OverlayController(
    private val context: Context,
    private val allowEndCall: Boolean,
    private val onDismiss: () -> Unit,
) {
    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var overlayView: View? = null
    private var titleTextView: TextView? = null
    private var bodyTextView: TextView? = null
    private var tipsTextView: TextView? = null
    private var languageToggleView: TextView? = null
    private var iconView: ImageView? = null
    private var isTamil: Boolean = true
    private var currentAlertType: String = ScamOverlayService.ALERT_TYPE_SCAM_CALL

    fun show(alertType: String) {
        currentAlertType = alertType

        if (overlayView != null) {
            updateWarningForAlertType()
            return
        }

        val wmLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        val root = FrameLayout(context).apply {
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.parseColor("#0B0F14"),
                    Color.parseColor("#14161D"),
                    Color.parseColor("#0A0B10")
                )
            )
        }

        // Top-right language pill
        val langPill = TextView(context).apply {
            textSize = 13f
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            setPadding(dp(14), dp(10), dp(14), dp(10))
            background = GradientDrawable().apply {
                cornerRadius = dpF(999)
                setColor(Color.parseColor("#1AFFFFFF"))
                setStroke(dp(1), Color.parseColor("#33FFFFFF"))
            }
            setOnClickListener {
                isTamil = !isTamil
                updateWarningForAlertType()
                updateLanguageButtonLabel()
            }
        }
        languageToggleView = langPill

        root.addView(
            langPill,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END
            }
        )

        // Center alert card
        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(22), dp(22), dp(22))
            gravity = Gravity.CENTER_HORIZONTAL
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.parseColor("#B00020"), Color.parseColor("#7A0015"))
            ).apply {
                cornerRadius = dpF(26)
                setStroke(dp(1), Color.parseColor("#33FFFFFF"))
            }
            elevation = dpF(10)
        }

        val headerRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val icon = ImageView(context).apply {
            setImageResource(android.R.drawable.ic_dialog_alert)
            this.layoutParams = LinearLayout.LayoutParams(dp(34), dp(34))
            setPadding(0, 0, dp(10), 0)
            tintTo(Color.WHITE)
        }
        iconView = icon

        val titlesCol = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            this.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val title = TextView(context).apply {
            textSize = 24f
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
        }
        titleTextView = title

        val subtitle = TextView(context).apply {
            textSize = 14.5f
            setTextColor(Color.parseColor("#E6FFFFFF"))
        }
        bodyTextView = subtitle

        titlesCol.addView(title)
        titlesCol.addView(subtitle)

        headerRow.addView(icon)
        headerRow.addView(titlesCol)

        val divider = View(context).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(Color.parseColor("#00FFFFFF"), Color.parseColor("#55FFFFFF"), Color.parseColor("#00FFFFFF"))
            )
        }

        val tips = TextView(context).apply {
            textSize = 15.5f
            setTextColor(Color.WHITE)
            setLineSpacing(dpF(3), 1.05f)
        }
        tipsTextView = tips

        val actionsRow = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        fun makePrimaryButton(text: String): Button =
            Button(context).apply {
                this.text = text
                textSize = 17f
                isAllCaps = false
                setTextColor(Color.parseColor("#0B0F14"))
                background = GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    intArrayOf(Color.parseColor("#FFFFFF"), Color.parseColor("#F3F3F3"))
                ).apply { cornerRadius = dpF(16) }
                setPadding(dp(16), dp(14), dp(16), dp(14))
            }

        fun makeSecondaryButton(text: String): Button =
            Button(context).apply {
                this.text = text
                textSize = 16f
                isAllCaps = false
                setTextColor(Color.WHITE)
                background = GradientDrawable().apply {
                    cornerRadius = dpF(16)
                    setColor(Color.parseColor("#22FFFFFF"))
                    setStroke(dp(1), Color.parseColor("#33FFFFFF"))
                }
                setPadding(dp(16), dp(14), dp(16), dp(14))
            }

        if (allowEndCall) {
            val endCallButton = makePrimaryButton("End call now").apply {
                setOnClickListener { endCurrentCallBestEffort() }
            }
            actionsRow.addView(
                endCallButton,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(14) }
            )
        }

        val okButton = makeSecondaryButton("Dismiss").apply {
            setOnClickListener { onDismiss() }
        }
        actionsRow.addView(
            okButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(12) }
        )

        card.addView(headerRow)
        card.addView(
            divider,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(1)
            ).apply {
                topMargin = dp(16)
                bottomMargin = dp(14)
            }
        )
        card.addView(tips)
        card.addView(actionsRow)

        root.addView(
            card,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        overlayView = root

        updateWarningForAlertType()
        updateLanguageButtonLabel()

        try {
            windowManager.addView(overlayView, wmLayoutParams)
        } catch (e: Exception) {
            Log.e("OverlayController", "Failed to add overlay", e)
            overlayView = null
            titleTextView = null
            bodyTextView = null
            tipsTextView = null
            languageToggleView = null
            iconView = null
        }
    }

    fun update(alertType: String) {
        currentAlertType = alertType
        updateWarningForAlertType()
    }

    fun dismiss() {
        try {
            overlayView?.let { windowManager.removeView(it) }
        } catch (e: Exception) {
            Log.e("OverlayController", "Error removing overlay", e)
        } finally {
            overlayView = null
            titleTextView = null
            bodyTextView = null
            tipsTextView = null
            languageToggleView = null
            iconView = null
        }
    }

    private fun updateWarningForAlertType() {
        val (title, subtitle, tips, tint) = if (isTamil) {
            when (currentAlertType) {
                ScamOverlayService.ALERT_TYPE_HIGH_RISK -> {
                    Quad(
                        "மோசடி எச்சரிக்கை",
                        "இது நிச்சயமாக மோசடி முயற்சி.",
                        "• OTP-ஐ யாரிடமும் கூறாதீர்கள்\n" +
                            "• வங்கி/UPI PIN/ATM விவரங்கள் ஒருபோதும் பகிர வேண்டாம்\n" +
                            "• உடனே அழைப்பை நிறுத்துங்கள்\n" +
                            "• தேவையானால் குடும்பத்தினரை தொடர்புகொள்ளுங்கள்",
                        Color.parseColor("#FF3B30")
                    )
                }

                ScamOverlayService.ALERT_TYPE_OTP_ONLY -> {
                    Quad(
                        "OTP பாதுகாப்பு",
                        "உங்கள் OTP வந்துள்ளது — பாதுகாப்பாக இருங்கள்.",
                        "• OTP / PIN / CVV யாருக்கும் கூற வேண்டாம்\n" +
                            "• வங்கி/அரசு/போலீஸ் யாரும் OTP கேட்க மாட்டார்கள்\n" +
                            "• சந்தேகம் இருந்தால், நிறுவனத்தின் அதிகாரப்பூர்வ எண்ணுக்கு திரும்ப அழையுங்கள்",
                        Color.parseColor("#FFB020")
                    )
                }

                else -> {
                    Quad(
                        "எச்சரிக்கை",
                        "சந்தேகமான நடவடிக்கை கண்டறியப்பட்டது.",
                        "• OTP அல்லது வங்கி விவரங்களை பகிர வேண்டாம்\n" +
                            "• அறியாத இணைப்புகளை திறக்க வேண்டாம்\n" +
                            "• அழைப்பாளர் அழுத்தம் கொடுத்தால் உடனே நிறுத்துங்கள்",
                        Color.parseColor("#FFB020")
                    )
                }
            }
        } else {
            when (currentAlertType) {
                ScamOverlayService.ALERT_TYPE_HIGH_RISK -> {
                    Quad(
                        "Confirmed scam alert",
                        "This is very likely a scam attempt.",
                        "• Never share OTP or PIN\n" +
                            "• Do not share bank/UPI/ATM details\n" +
                            "• End the call immediately\n" +
                            "• Call the official support number yourself",
                        Color.parseColor("#FF3B30")
                    )
                }

                ScamOverlayService.ALERT_TYPE_OTP_ONLY -> {
                    Quad(
                        "OTP safety alert",
                        "An OTP was detected — stay safe.",
                        "• Never share OTP / PIN / CVV\n" +
                            "• Banks/government never ask for OTP\n" +
                            "• If unsure, call back via the official number",
                        Color.parseColor("#FFB020")
                    )
                }

                else -> {
                    Quad(
                        "Security warning",
                        "Suspicious activity detected.",
                        "• Don’t share OTP or banking info\n" +
                            "• Don’t open unknown links\n" +
                            "• If pressured, hang up immediately",
                        Color.parseColor("#FFB020")
                    )
                }
            }
        }

        titleTextView?.text = title
        bodyTextView?.text = subtitle
        tipsTextView?.text = tips
        iconView?.tintTo(tint)
    }

    private fun updateLanguageButtonLabel() {
        languageToggleView?.text = if (isTamil) {
            "தமிழ்  ➜  English"
        } else {
            "English  ➜  தமிழ்"
        }
    }

    private fun endCurrentCallBestEffort() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            try {
                val success = telecomManager.endCall()
                if (!success) {
                    Toast.makeText(
                        context,
                        "Unable to end call automatically. Please hang up.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (_: SecurityException) {
                Toast.makeText(
                    context,
                    "Permission required to end calls. Please hang up manually.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                context,
                "Please hang up the call from the phone app.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            context.resources.displayMetrics
        ).toInt()

    private fun dpF(value: Int): Float = dp(value).toFloat()

    private fun ImageView.tintTo(color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            colorFilter = BlendModeColorFilter(color, BlendMode.SRC_IN)
        } else {
            @Suppress("DEPRECATION")
            setColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
    }

    private data class Quad(
        val first: String,
        val second: String,
        val third: String,
        val fourth: Int,
    )
}

