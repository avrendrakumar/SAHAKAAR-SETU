package com.example.engine

import com.example.data.model.WorkerProfileEntity
import kotlin.math.*

data class WorkerMatchResult(
    val worker: WorkerProfileEntity,
    val overallScore: Int, // 0 - 100%
    val skillMatch: Int, // 0 - 100%
    val distanceMatch: Int, // 0 - 100%
    val distanceKm: Double,
    val availabilityStatus: String, // "Available", "Busy", "Offline"
    val availabilityScore: Int,
    val reliabilityMatch: Int, // 0 - 100%
    val experienceYears: Int,
    val experienceScore: Int,
    val workloadLabel: String, // "Optimal", "Moderate", "High Load"
    val workloadScore: Int
)

object WorkerMatchingEngine {

    fun matchWorkers(
        workers: List<WorkerProfileEntity>,
        requiredSkill: String,
        targetLat: Double = 25.6100,
        targetLon: Double = 85.1415,
        isEmergency: Boolean = false
    ): List<WorkerMatchResult> {
        return workers.map { worker ->
            // 1. Skill Compatibility (30%)
            val skillScore = when {
                worker.primarySkill.equals(requiredSkill, ignoreCase = true) -> 98
                worker.secondarySkills.contains(requiredSkill, ignoreCase = true) -> 78
                worker.primarySkill.contains(requiredSkill, ignoreCase = true) -> 88
                else -> 40
            }

            // 2. Distance (20%)
            val distanceKm = calculateDistanceKm(targetLat, targetLon, worker.latitude, worker.longitude)
            val distanceScore = when {
                distanceKm <= 2.5 -> 96
                distanceKm <= 5.0 -> 88
                distanceKm <= 10.0 -> 76
                distanceKm <= 20.0 -> 60
                else -> 45
            }

            // 3. Availability (15%)
            val availStatus = when {
                !worker.isOnline -> "Offline"
                worker.isBusy -> "Busy"
                else -> "Available"
            }
            val availScore = when (availStatus) {
                "Available" -> 98
                "Busy" -> if (isEmergency) 20 else 55
                else -> 10
            }

            // 4. Reliability (15%)
            val reliabilityScore = worker.reliabilityScore.coerceIn(50, 100)

            // 5. Experience (10%)
            val expScore = (worker.experienceYears * 7 + 25).coerceIn(40, 100)

            // 6. Workload Balance (10%) - Fair Work Distribution
            // Workers with lower daily earnings get prioritized for equitable distribution
            val (workloadLabel, workloadScore) = when {
                worker.dailyEarnings < 1500.0 && !worker.isBusy -> Pair("Optimal", 96)
                worker.dailyEarnings < 2500.0 -> Pair("Moderate", 80)
                else -> Pair("High Load", 55)
            }

            // Weighted aggregation according to formula:
            // Skill: 30%, Distance: 20%, Availability: 15%, Reliability: 15%, Experience: 10%, Workload: 10%
            val overall = (
                skillScore * 0.30 +
                distanceScore * 0.20 +
                availScore * 0.15 +
                reliabilityScore * 0.15 +
                expScore * 0.10 +
                workloadScore * 0.10
            ).roundToInt().coerceIn(10, 99)

            WorkerMatchResult(
                worker = worker,
                overallScore = overall,
                skillMatch = skillScore,
                distanceMatch = distanceScore,
                distanceKm = (distanceKm * 10).roundToInt() / 10.0,
                availabilityStatus = availStatus,
                availabilityScore = availScore,
                reliabilityMatch = reliabilityScore,
                experienceYears = worker.experienceYears,
                experienceScore = expScore,
                workloadLabel = workloadLabel,
                workloadScore = workloadScore
            )
        }.sortedWith(
            compareByDescending<WorkerMatchResult> { if (isEmergency && it.availabilityStatus == "Available") 1 else 0 }
                .thenByDescending { it.overallScore }
        )
    }

    private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}
