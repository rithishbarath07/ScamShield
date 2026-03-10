package com.rithish.scamshield.logic

/**
 * Tracks current call state for use by SMS / fraud detection logic.
 */
object CallStateTracker {

    @Volatile
    var isCallActive: Boolean = false
        private set

    @Volatile
    var isScamCallActive: Boolean = false
        private set

    @Volatile
    var lastIncomingNumber: String? = null
        private set

    fun onIncomingCallRinging(normalizedNumber: String?, isScam: Boolean) {
        lastIncomingNumber = normalizedNumber
        isScamCallActive = isScam
        isCallActive = true
    }

    fun onCallOffhook() {
        // Call has been answered or is active.
        isCallActive = true
    }

    fun onCallIdle() {
        // Call ended or no active call.
        isCallActive = false
        isScamCallActive = false
        lastIncomingNumber = null
    }
}

