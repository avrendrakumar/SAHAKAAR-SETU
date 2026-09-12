package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val name: String,
    val role: String, // CUSTOMER, WORKER, COOPERATIVE_ADMIN, FEDERATION_ADMIN, SUPER_ADMIN
    val phone: String,
    val city: String,
    val cooperativeId: String? = null,
    val customIdTag: String = "", // e.g. "SS-CUS-000001", "SS-WRK-000001"
    val state: String = "Bihar",
    val district: String = "Patna",
    val village: String = "",
    val passwordHash: String = "",
    val isMobileVerified: Boolean = true,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cooperatives")
data class CooperativeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val registrationNumber: String,
    val district: String,
    val state: String = "Bihar",
    val contactPhone: String,
    val totalWorkers: Int = 0,
    val rating: Float = 4.8f,
    val establishedYear: Int = 2018
)

@Entity(tableName = "workers")
data class WorkerProfileEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val workerIdTag: String, // e.g. "SS-PAT-EL-0142"
    val name: String,
    val phone: String,
    val cooperativeId: String,
    val cooperativeName: String,
    val primarySkill: String,
    val secondarySkills: String, // Comma separated: "Wiring, Solar Inverter, Appliance Repair"
    val experienceYears: Int,
    val verificationStatus: String, // "VERIFIED", "PENDING", "SUSPENDED"
    val certifications: String, // "NSDC Level 4, ITI Certified Electrician"
    val customerRating: Float,
    val ratingCount: Int,
    val completedJobs: Int,
    val reliabilityScore: Int, // 0 - 100%
    val dailyWageRate: Double, // in INR
    val isAvailable: Boolean = true,
    val isBusy: Boolean = false,
    val isEmergencyReady: Boolean = true,
    val lat: Double,
    val lng: Double,
    val city: String,
    val zone: String,
    val photoAvatarId: Int = 0
)

@Entity(tableName = "service_categories")
data class ServiceCategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val hindiName: String,
    val iconName: String,
    val basePrice: Double,
    val description: String,
    val isEmergencyAvailable: Boolean = true
)

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
    val bookingNumber: String, // e.g. "BK-2026-081"
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val workerId: String,
    val workerName: String,
    val cooperativeId: String,
    val serviceCategory: String,
    val problemDescription: String,
    val urgency: String, // "NORMAL", "HIGH", "EMERGENCY"
    val isEmergency: Boolean = false,
    val locationAddress: String,
    val city: String,
    val scheduledDate: String,
    val scheduledTime: String,
    val status: String, // "CREATED", "ASSIGNED", "ON_THE_WAY", "ARRIVED", "WORK_STARTED", "COMPLETED", "CANCELLED"
    val laborCost: Double,
    val materialCost: Double = 0.0,
    val platformFee: Double = 25.0,
    val totalAmount: Double,
    val paymentStatus: String = "PENDING", // "PENDING", "PAID"
    val paymentMethod: String? = null, // "UPI", "CARD", "CASH"
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey val id: String,
    val bookingId: String,
    val transactionRef: String,
    val amount: Double,
    val paymentMethod: String,
    val status: String = "SUCCESS",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey val id: String,
    val invoiceNumber: String,
    val bookingId: String,
    val customerName: String,
    val workerName: String,
    val cooperativeName: String,
    val serviceName: String,
    val laborCost: Double,
    val materialCost: Double,
    val platformFee: Double,
    val totalAmount: Double,
    val paymentStatus: String,
    val paymentMethod: String,
    val issuedDate: String
)

@Entity(tableName = "ratings")
data class RatingEntity(
    @PrimaryKey val id: String,
    val bookingId: String,
    val workerId: String,
    val customerId: String,
    val customerName: String,
    val rating: Int,
    val reviewText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "disputes")
data class DisputeEntity(
    @PrimaryKey val id: String,
    val bookingId: String,
    val customerId: String,
    val customerName: String,
    val workerId: String,
    val reason: String,
    val description: String,
    val status: String = "OPEN", // "OPEN", "UNDER_REVIEW", "RESOLVED", "REJECTED"
    val adminResponse: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "welfare_wallets")
data class WelfareWalletEntity(
    @PrimaryKey val workerId: String,
    val balance: Double,
    val insuranceCovered: String = "PM Suraksha Bima Yojana - ₹2,00,000",
    val trainingCredits: Int = 120,
    val emergencyAssistanceFund: Double = 5000.0,
    val totalContributions: Double = 3400.0
)

@Entity(tableName = "welfare_transactions")
data class WelfareTransactionEntity(
    @PrimaryKey val id: String,
    val workerId: String,
    val amount: Double,
    val type: String, // "COOP_CONTRIBUTION", "ACCIDENT_BENEFIT", "PENSION_ACCRUAL", "SKILL_STIPEND"
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val role: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val actionType: String? = null
)

@Entity(tableName = "demand_forecasts")
data class DemandForecastEntity(
    @PrimaryKey val id: String,
    val serviceCategory: String,
    val district: String,
    val predictedDemand: Int,
    val availableWorkers: Int,
    val shortage: Int,
    val recommendation: String
)

@Entity(tableName = "institutional_bookings")
data class InstitutionalBookingEntity(
    @PrimaryKey val id: String,
    val institutionName: String,
    val institutionType: String, // "SCHOOL", "HOSPITAL", "OFFICE", "FACTORY", "HOTEL", "SOCIETY"
    val requiredSkill: String,
    val workerCount: Int,
    val durationDays: Int,
    val locationCity: String,
    val contactPerson: String,
    val contactPhone: String,
    val status: String = "PENDING_ALLOCATION", // "PENDING_ALLOCATION", "ALLOCATED", "IN_PROGRESS", "COMPLETED"
    val cooperativeId: String,
    val estimatedBudget: Double,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val actorUserId: String,
    val action: String,
    val targetUserId: String? = null,
    val metadata: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "professions")
data class ProfessionEntity(
    @PrimaryKey val id: String,
    val nameEn: String,
    val nameHi: String,
    val iconName: String = "ic_skill",
    val isActive: Boolean = true
)

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey val id: String,
    val state: String = "Bihar",
    val district: String,
    val cityOrTown: String,
    val villageOrArea: String,
    val pincode: String = "",
    val latitude: Double = 25.5941,
    val longitude: Double = 85.1376
)

