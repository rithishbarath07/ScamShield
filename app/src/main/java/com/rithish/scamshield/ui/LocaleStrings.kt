package com.rithish.scamshield.ui

/**
 * Tamil (default) and English strings for the app.
 * Use isTamil to switch; persist in preferences.
 */
data class LocaleStrings(
    val appName: String,
    val protectionActive: String,
    val protectionActiveSubtitle: String,
    val languageTamil: String,
    val languageEnglish: String,
    val switchToEnglish: String,
    val switchToTamil: String,
    val statusProtected: String,
    val statusMonitoring: String
)

val TamilStrings = LocaleStrings(
    appName = "ஸ்காம் ஷீல்ட்",
    protectionActive = "பாதுகாப்பு செயலில்",
    protectionActiveSubtitle = "அழைப்புகள் மற்றும் செய்திகள் கண்காணிக்கப்படுகின்றன",
    languageTamil = "தமிழ்",
    languageEnglish = "English",
    switchToEnglish = "English",
    switchToTamil = "தமிழ்",
    statusProtected = "நீங்கள் பாதுகாக்கப்படுகிறீர்கள்",
    statusMonitoring = "கண்காணிப்பு செயலில்"
)

val EnglishStrings = LocaleStrings(
    appName = "ScamShield",
    protectionActive = "Protection Active",
    protectionActiveSubtitle = "Calls and messages are being monitored",
    languageTamil = "தமிழ்",
    languageEnglish = "English",
    switchToEnglish = "English",
    switchToTamil = "தமிழ்",
    statusProtected = "You are protected",
    statusMonitoring = "Monitoring active"
)
