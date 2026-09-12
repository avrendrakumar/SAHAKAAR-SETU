package com.example.data.auth

import java.security.MessageDigest

data class PasswordStrengthResult(
    val score: Int, // 0..3
    val label: String,
    val colorHex: Long
)

object PasswordSecurity {

    private const val SALT = "SahakaarSetu_SecureSalt_2026_BiharCoop"

    /**
     * Computes a SHA-256 hash with a secure salt.
     * Passwords are never stored in plain text.
     */
    fun hashPassword(password: String): String {
        val saltedPassword = "$SALT:$password:$SALT"
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(saltedPassword.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies plain text password against stored hash.
     */
    fun verifyPassword(password: String, storedHash: String): Boolean {
        if (storedHash.isBlank()) return false
        val computedHash = hashPassword(password)
        return computedHash == storedHash
    }

    /**
     * Validates password strength:
     * At least 6 characters.
     */
    fun validatePasswordStrength(password: String): Pair<Boolean, String> {
        if (password.length < 6) {
            return Pair(false, "Password must be at least 6 characters long.")
        }
        return Pair(true, "Strong password.")
    }

    fun calculateStrength(password: String): PasswordStrengthResult {
        if (password.isBlank()) {
            return PasswordStrengthResult(0, "Empty", 0xFF8E9099)
        }
        var score = 0
        if (password.length >= 6) score++
        if (password.length >= 8) score++
        if (password.any { it.isUpperCase() } && password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        val normalized = score.coerceIn(0, 3)
        return when (normalized) {
            0 -> PasswordStrengthResult(0, "Weak", 0xFFBA1A1A)
            1 -> PasswordStrengthResult(1, "Fair", 0xFFFF9800)
            2 -> PasswordStrengthResult(2, "Good", 0xFF2196F3)
            else -> PasswordStrengthResult(3, "Strong", 0xFF7DDC9A)
        }
    }
}
