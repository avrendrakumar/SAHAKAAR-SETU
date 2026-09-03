package com.example

import com.example.data.database.SeedData
import com.example.engine.AiServiceClassifier
import com.example.engine.WorkerMatchingEngine
import org.junit.Assert.*
import org.junit.Test

class SahakaarEngineUnitTest {

    @Test
    fun `ai service classifier correctly classifies hindi plumbing problem`() {
        val input = "Mere ghar ka pipe leak ho raha hai"
        val result = AiServiceClassifier.classifyService(input)

        assertEquals("Plumbing", result.serviceName)
        assertEquals("Plumbing", result.requiredSkill)
        assertTrue(result.isEmergency)
        assertTrue(result.urgency.contains("High") || result.urgency.contains("Emergency"))
    }

    @Test
    fun `ai service classifier correctly identifies electrical sparking emergency`() {
        val input = "Switch board sparking and tripping MCB urgent"
        val result = AiServiceClassifier.classifyService(input)

        assertEquals("Electrical", result.serviceName)
        assertTrue(result.isEmergency)
    }

    @Test
    fun `worker matching engine ranks matching skill and availability higher`() {
        val workers = SeedData.generateWorkers()
        val results = WorkerMatchingEngine.matchWorkers(
            workers = workers,
            requiredSkill = "Plumbing",
            targetLat = 25.6100,
            targetLon = 85.1415,
            isEmergency = false
        )

        assertFalse(results.isEmpty())
        val topWorker = results.first()
        // Top worker should have high skill match and good overall match
        assertTrue("Overall score should be high", topWorker.overallScore >= 70)
        assertEquals("Primary skill should be Plumbing", "Plumbing", topWorker.worker.primarySkill)
    }
}
