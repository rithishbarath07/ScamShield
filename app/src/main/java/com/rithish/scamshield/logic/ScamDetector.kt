package com.rithish.scamshield.logic

/**
 * Simple local scam-number detector.
 *
 * Numbers are stored in normalized form (last 10 digits, no country code or separators).
 */
object ScamDetector {

    // Example local scam numbers. Always store these as the last 10 digits only.
    private val scamNumbers: Set<String> = setOf(
        "9363596588",
    )

    /**
     * Returns true if the given raw phone number matches a known scam number.
     */
    fun isScamNumber(rawNumber: String?): Boolean {
        if (rawNumber.isNullOrBlank()) return false
        val cleaned = normalizeNumber(rawNumber)
        return cleaned != null && scamNumbers.contains(cleaned)
    }

    /**
     * Normalize a raw phone number to a consistent 10‑digit format.
     *
     * - Removes +91 (or other leading +countryCode patterns)
     * - Removes spaces and hyphens
     * - Keeps only digits and returns the last 10 digits, if available
     */
    fun normalizeNumber(rawNumber: String?): String? {
        if (rawNumber.isNullOrBlank()) return null

        // Drop leading +91 specifically for Indian numbers, if present
        var number = rawNumber.replace("+91", "")

        // Remove all non-digit characters (spaces, hyphens, etc.)
        number = number.filter { it.isDigit() }

        // Only keep last 10 digits if we have at least 10
        return if (number.length >= 10) {
            number.takeLast(10)
        } else {
            null
        }
    }
}

