package com.example.domain.matching

import com.example.data.model.BookingEntity
import com.example.data.model.WorkerProfileEntity
import kotlin.math.*

data class MatchBreakdown(
    val worker: WorkerProfileEntity,
    val skillCompatibilityScore: Int, // 0 - 100
    val distanceScore: Int, // 0 - 100
    val estimatedDistanceKm: Double,
    val availabilityScore: Int, // 0 - 100
    val reliabilityScore: Int, // 0 - 100
    val experienceScore: Int, // 0 - 100
    val workloadBalanceScore: Int, // 0 - 100
    val workloadLabel: String, // "Optimal", "Good", "Heavy"
    val overallMatchScore: Int, // 0 - 100
    val estimatedEtaMinutes: Int
)

object IntelligentWorkerMatcher {

    /**
     * Exact 6-Factor Weighted Algorithm:
     * - Skill Compatibility: 30%
     * - Distance: 20%
     * - Availability: 15%
     * - Reliability: 15%
     * - Experience: 10%
     * - Workload Balance: 10%
     */
    fun matchWorkers(
        requiredServiceCategory: String,
        targetLat: Double,
        targetLng: Double,
        isEmergency: Boolean,
        workers: List<WorkerProfileEntity>,
        activeBookings: List<BookingEntity>
    ): List<MatchBreakdown> {
        return workers
            .filter { it.verificationStatus == "VERIFIED" && it.isAvailable }
            .map { worker ->
                // 1. Skill Compatibility (30%)
                val skillScore = calculateSkillScore(worker, requiredServiceCategory)

                // 2. Distance (20%)
                val distanceKm = calculateHaversineDistance(targetLat, targetLng, worker.lat, worker.lng)
                val distanceScore = calculateDistanceScore(distanceKm)

                // 3. Availability (15%)
                val availScore = if (worker.isAvailable && !worker.isBusy) {
                    if (isEmergency && worker.isEmergencyReady) 100 else 90
                } else if (!worker.isAvailable) {
                    20
                } else {
                    40 // busy on another job
                }

                // 4. Reliability (15%)
                val relScore = worker.reliabilityScore.coerceIn(0, 100)

                // 5. Experience (10%)
                // 10+ years gets 100%, 5 years gets 75%, 2 years gets 50%
                val expScore = ((worker.experienceYears.toDouble() / 10.0) * 100.0).toInt().coerceIn(30, 100)

                // 6. Workload Balance / Fair Distribution (10%)
                val activeJobsForWorker = activeBookings.count {
                    it.workerId == worker.id && it.status in listOf("ASSIGNED", "ON_THE_WAY", "ARRIVED", "WORK_STARTED")
                }
                val (workloadScore, workloadLabel) = when {
                    activeJobsForWorker == 0 -> Pair(100, "Optimal")
                    activeJobsForWorker == 1 -> Pair(75, "Good")
                    activeJobsForWorker == 2 -> Pair(45, "Moderate")
                    else -> Pair(20, "Heavy") // Fair distribution penalty: prevents same worker from receiving every job
                }

                // Overall Weighted Calculation
                val overall = (
                    (skillScore * 0.30) +
                    (distanceScore * 0.20) +
                    (availScore * 0.15) +
                    (relScore * 0.15) +
                    (expScore * 0.10) +
                    (workloadScore * 0.10)
                ).roundToInt().coerceIn(10, 99)

                val eta = (distanceKm * 4.0 + 8.0).roundToInt().coerceIn(10, 60)

                MatchBreakdown(
                    worker = worker,
                    skillCompatibilityScore = skillScore,
                    distanceScore = distanceScore,
                    estimatedDistanceKm = (distanceKm * 10).roundToInt() / 10.0,
                    availabilityScore = availScore,
                    reliabilityScore = relScore,
                    experienceScore = expScore,
                    workloadBalanceScore = workloadScore,
                    workloadLabel = workloadLabel,
                    overallMatchScore = overall,
                    estimatedEtaMinutes = eta
                )
            }
            .sortedWith(
                compareByDescending<MatchBreakdown> {
                    if (isEmergency) (it.overallMatchScore + if (it.worker.isEmergencyReady) 20 else 0) else it.overallMatchScore
                }
            )
    }

    private fun calculateSkillScore(worker: WorkerProfileEntity, targetCategory: String): Int {
        if (worker.primarySkill.equals(targetCategory, ignoreCase = true)) {
            return 95
        }
        val targetLower = targetCategory.lowercase()
        if (worker.secondarySkills.lowercase().contains(targetLower)) {
            return 85
        }
        // General artisan cross-competency
        if (targetLower.contains("clean") && worker.primarySkill.contains("Labour", true)) return 75
        if (targetLower.contains("appliance") && worker.primarySkill.contains("Electrical", true)) return 88
        if (targetLower.contains("solar") && worker.primarySkill.contains("Electrical", true)) return 92
        return 40
    }

    private fun calculateDistanceScore(distanceKm: Double): Int {
        return when {
            distanceKm <= 2.0 -> 98
            distanceKm <= 5.0 -> 88
            distanceKm <= 10.0 -> 72
            distanceKm <= 18.0 -> 55
            else -> 35
        }
    }

    private fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
