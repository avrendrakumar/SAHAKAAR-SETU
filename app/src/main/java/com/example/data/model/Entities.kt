package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole(val displayName: String, val hindiName: String) {
    CUSTOMER("Customer", "ग्राहक"),
    WORKER("Worker", "श्रमिक / कारीगर"),
    COOPERATIVE_ADMIN("Cooperative Admin", "सहकारी समिति प्रबंधक"),
    FEDERATION_ADMIN("Federation Admin", "महासंघ प्रशासक"),
    SUPER_ADMIN("Super Admin", "मुख्य प्रशासक")
}

@Entity(tableName = "cooperatives")
data class CooperativeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val registrationNumber: String,
    val district: String,
    val state: String = "Bihar",
    val phone: String,
    val email: String,
    val address: String,
    val rating: Float = 4.8f,
    val latitude: Double,
    val longitude: Double
)

@Entity(tableName = "workers")
data class WorkerProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workerCode: String, // e.g. SS-PAT-2024-001
    val name: String,
    val phone: String,
    val email: String,
    val cooperativeId: Long,
    val cooperativeName: String,
    val primarySkill: String,
    val secondarySkills: String, // Comma-separated
    val experienceYears: Int,
    val rating: Float = 4.8f,
    val reviewCount: Int = 24,
    val reliabilityScore: Int = 92, // 0-100%
    val isVerified: Boolean = true,
    val verificationDate: String = "15 Jan 2024",
    val certList: String = "NSDC Level-4 Certified, Skill India Mission, Cooperative Safety Protocol",
    val isOnline: Boolean = true,
    val isBusy: Boolean = false,
    val isEmergencyReady: Boolean = true,
    val completedJobs: Int = 142,
    val welfareBalance: Double = 4250.0,
    val dailyEarnings: Double = 1850.0,
    val hourlyRate: Double = 250.0,
    val district: String = "Patna",
    val latitude: Double = 25.6100,
    val longitude: Double = 85.1415,
    val profilePhotoUrl: String = ""
)

@Entity(tableName = "service_categories")
data class ServiceCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val hindiName: String,
    val iconName: String,
    val basePrice: Double,
    val description: String,
    val isEmergencyAvailable: Boolean = true
)

enum class BookingStatus(val displayName: String, val hindiName: String) {
    PENDING("Pending", "लंबित"),
    ASSIGNED("Assigned", "आवंटित"),
    ACCEPTED("Accepted", "स्वीकृत"),
    ON_THE_WAY("On The Way", "रास्ते में"),
    ARRIVED("Arrived", "पहुंच गए"),
    IN_PROGRESS("In Progress", "कार्य प्रगति पर"),
    COMPLETED("Completed", "पूर्ण"),
    CANCELLED("Cancelled", "रद्द")
}

enum class PaymentStatus(val displayName: String) {
    PENDING("Payment Pending"),
    PAID_UPI("Paid via UPI"),
    PAID_CARD("Paid via Card"),
    PAID_CASH("Paid via Cash")
}

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookingCode: String, // e.g. BK-8821
    val customerId: String = "customer_1",
    val customerName: String,
    val customerPhone: String,
    val serviceName: String,
    val problemDescription: String,
    val address: String,
    val district: String,
    val isEmergency: Boolean = false,
    val status: BookingStatus = BookingStatus.PENDING,
    val workerId: Long? = null,
    val workerName: String? = null,
    val workerPhone: String? = null,
    val cooperativeId: Long? = null,
    val cooperativeName: String? = null,
    val scheduledDate: String,
    val scheduledTime: String,
    val labourCost: Double = 350.0,
    val materialCost: Double = 0.0,
    val platformFee: Double = 35.0,
    val totalAmount: Double = 385.0,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val paymentMethod: String = "",
    val paymentTransactionId: String = "",
    val isRated: Boolean = false,
    val ratingGiven: Float = 0f,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "welfare_transactions")
data class WelfareTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workerId: Long,
    val type: String, // "Cooperative Contribution", "Health Insurance Cover", "Training Credit", "Emergency Aid"
    val amount: Double,
    val isCredit: Boolean = true,
    val description: String,
    val dateString: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "ratings")
data class RatingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookingId: Long,
    val workerId: Long,
    val customerName: String,
    val rating: Float,
    val review: String,
    val dateString: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "disputes")
data class DisputeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookingId: Long,
    val bookingCode: String,
    val customerName: String,
    val workerName: String,
    val reason: String,
    val description: String,
    val status: String = "OPEN", // OPEN, UNDER_REVIEW, RESOLVED, REJECTED
    val adminResponse: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "institutional_bookings")
data class InstitutionalBookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val institutionName: String,
    val institutionType: String, // School, Hospital, Office, Factory, Hotel, Apartment Society, Government
    val contactPerson: String,
    val phone: String,
    val tradeRequired: String,
    val workerCount: Int,
    val durationDays: Int,
    val location: String,
    val status: String = "PENDING", // PENDING, ALLOCATED, IN_PROGRESS, COMPLETED
    val estimatedBudget: Double,
    val cooperativeAllocated: String = "Patna Shramik Sahakari Samiti",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "demand_forecasts")
data class DemandForecastEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val serviceName: String,
    val zone: String,
    val predictedDemand: Int,
    val availableWorkers: Int,
    val shortage: Int,
    val recommendation: String
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipientRole: String, // CUSTOMER, WORKER, COOPERATIVE_ADMIN, ALL
    val title: String,
    val message: String,
    val timeAgo: String,
    val isRead: Boolean = false,
    val relatedBookingId: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)
