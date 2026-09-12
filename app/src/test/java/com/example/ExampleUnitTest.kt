package com.example

import com.example.data.ai.AiServiceClassifier
import com.example.data.database.DatabaseSeeder
import com.example.domain.matching.IntelligentWorkerMatcher
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testIntelligentWorkerMatcherWeights() {
    val workers = DatabaseSeeder.getInitialWorkers()
    val matches = IntelligentWorkerMatcher.matchWorkers(
      requiredServiceCategory = "Electrical",
      targetLat = 25.6110,
      targetLng = 85.1440,
      isEmergency = false,
      workers = workers,
      activeBookings = emptyList()
    )

    assertTrue("Matches should not be empty", matches.isNotEmpty())
    val topMatch = matches.first()
    assertEquals("Top match should be Electrical", "Electrical", topMatch.worker.primarySkill)
    assertTrue("Match score should be between 70 and 100", topMatch.overallMatchScore in 70..100)
    assertTrue("Skill score should be high", topMatch.skillCompatibilityScore >= 90)
  }

  @Test
  fun testAiClassifierLocalFallback() = runBlocking {
    val plumbingResult = AiServiceClassifier.classifyProblem("Mere ghar ka pipe leak ho raha hai aur paani beh raha hai")
    assertEquals("Plumbing", plumbingResult.serviceCategory)
    assertTrue("Confidence should be high", plumbingResult.confidence > 0.8f)

    val electricalResult = AiServiceClassifier.classifyProblem("Ceiling fan mein spark ho raha hai aur switch trip kar gaya")
    assertEquals("Electrical", electricalResult.serviceCategory)
  }

  @Test
  fun testPasswordSecurityHashingAndVerification() {
    val plainPassword = "SecurePassword2026!"
    val hash1 = com.example.data.auth.PasswordSecurity.hashPassword(plainPassword)
    val hash2 = com.example.data.auth.PasswordSecurity.hashPassword(plainPassword)

    assertNotEquals("Hash must not be plain text", plainPassword, hash1)
    assertEquals("Hash should be deterministic with salt", hash1, hash2)
    assertTrue("Verification should succeed with correct password", com.example.data.auth.PasswordSecurity.verifyPassword(plainPassword, hash1))
    assertFalse("Verification should fail with wrong password", com.example.data.auth.PasswordSecurity.verifyPassword("WrongPassword", hash1))
  }

  @Test
  fun testPasswordStrengthMeter() {
    val weak = com.example.data.auth.PasswordSecurity.calculateStrength("123")
    val strong = com.example.data.auth.PasswordSecurity.calculateStrength("Sahakaar@2026Secure!")
    assertTrue("Weak password score should be <= 1", weak.score <= 1)
    assertTrue("Strong password score should be >= 2", strong.score >= 2)
  }

  @Test
  fun testOTPGenerationAndVerification() {
    val testMobile = "+919835012345"
    val generatedOtp = com.example.data.auth.OTPServiceManager.instance.generateOtp(testMobile)

    assertEquals("OTP should be 6 digits", 6, generatedOtp.length)
    assertTrue("OTP should be numeric", generatedOtp.all { it.isDigit() })

    val isSuccess = com.example.data.auth.OTPServiceManager.instance.verifyOtp(testMobile, generatedOtp)
    assertTrue("Generated OTP must verify successfully", isSuccess)

    val isFail = com.example.data.auth.OTPServiceManager.instance.verifyOtp(testMobile, "000000")
    assertFalse("Incorrect OTP must fail verification", isFail)
  }
}

