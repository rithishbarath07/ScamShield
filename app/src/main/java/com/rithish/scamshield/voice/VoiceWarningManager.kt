package com.rithish.scamshield.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/**
 * Central manager for Tamil voice alerts.
 */
object VoiceWarningManager : TextToSpeech.OnInitListener {

    private const val TAG = "VoiceWarningManager"
    private const val TAMIL_OTP_WARNING_TEXT =
        "கவனம். OTP வந்துள்ளது. இந்த குறியீட்டை யாரிடமும் பகிரக் கூடாது. வங்கி, அரசு, போலீஸ் யாரும் OTP கேட்க மாட்டார்கள்."
    private const val TAMIL_HIGH_RISK_WARNING_TEXT =
        "கவனம். இது மோசடி அழைப்பு தான். OTP, வங்கி விவரங்களை எவருக்கும் கூறக் கூடாது. உடனே அழைப்பை நிறுத்துங்கள்."
    private const val TAMIL_SUSPECTED_CALL_WARNING_TEXT =
        "கவனம். இது மோசடி அழைப்பு ஆக இருக்கலாம். OTP அல்லது வங்கி விவரங்களை ஒருபோதும் கூறக் கூடாது."

    @Volatile
    private var tts: TextToSpeech? = null

    @Volatile
    private var isInitialized: Boolean = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val locale = Locale("ta", "IN")
            val result = tts?.setLanguage(locale)

            isInitialized = result != TextToSpeech.LANG_MISSING_DATA &&
                result != TextToSpeech.LANG_NOT_SUPPORTED

            Log.d(TAG, "TTS initialized, Tamil support: $isInitialized")
        } else {
            Log.e(TAG, "TTS initialization failed with status: $status")
            isInitialized = false
        }
    }

    fun speakOtpWarning(context: Context) {
        speakInternal(context, TAMIL_OTP_WARNING_TEXT)
    }

    fun speakSuspectedCallWarning(context: Context) {
        speakInternal(context, TAMIL_SUSPECTED_CALL_WARNING_TEXT)
    }

    fun speakHighRiskWarning(context: Context) {
        speakInternal(context, TAMIL_HIGH_RISK_WARNING_TEXT)
    }

    private fun speakInternal(context: Context, text: String) {
        ensureTtsInitialized(context.applicationContext)

        if (!isInitialized) {
            Log.e(TAG, "TTS not initialized or Tamil not supported")
            return
        }

        val engine = tts ?: return
        engine.stop()
        engine.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "scam_${System.currentTimeMillis()}"
        )
    }

    private fun ensureTtsInitialized(appContext: Context) {
        if (tts != null) return

        synchronized(this) {
            if (tts == null) {
                tts = TextToSpeech(appContext, this)
            }
        }
    }

    /**
     * Optional: call from Application.onTerminate() or when no longer needed.
     */
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}

