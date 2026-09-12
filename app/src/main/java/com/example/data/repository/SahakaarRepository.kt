package com.example.data.repository

import android.util.Log
import com.example.data.database.AppDatabase
import com.example.data.database.DatabaseSeeder
import com.example.data.model.*
import com.example.data.supabase.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID

class SahakaarRepository(private val database: AppDatabase) {

    val allCooperatives: Flow<List<CooperativeEntity>> = database.cooperativeDao().getAllCooperatives()
    val allCategories: Flow<List<ServiceCategoryEntity>> = database.serviceCategoryDao().getAllCategories()
    val allWorkers: Flow<List<WorkerProfileEntity>> = database.workerDao().getAllWorkers()
    val allBookings: Flow<List<BookingEntity>> = database.bookingDao().getAllBookings()
    val allForecasts: Flow<List<DemandForecastEntity>> = database.demandForecastDao().getAllForecasts()
    val allInstitutionalBookings: Flow<List<InstitutionalBookingEntity>> = database.institutionalBookingDao().getAllInstitutionalBookings()
    val allDisputes: Flow<List<DisputeEntity>> = database.disputeDao().getAllDisputes()
    val allProfessions: Flow<List<ProfessionEntity>> = database.professionDao().getAllProfessions()
    val allAuditLogs: Flow<List<AuditLogEntity>> = database.auditLogDao().getAllAuditLogs()
    val allUsers: Flow<List<UserEntity>> = database.userDao().getAllUsers()

    suspend fun ensureDataSeeded() = withContext(Dispatchers.IO) {
        val workers = database.workerDao().getAllWorkers().first()
        if (workers.isEmpty()) {
            AppDatabase.populateDatabase(database)
        }
        try {
            syncLiveWorkersFromSupabase()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {}
        try {
            syncBookingsFromSupabase()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {}
    }

    suspend fun getUserByEmail(email: String): UserEntity? = withContext(Dispatchers.IO) {
        database.userDao().getUserByEmail(email)
    }

    suspend fun getUserByPhone(phone: String): UserEntity? = withContext(Dispatchers.IO) {
        database.userDao().getUserByPhone(phone)
    }

    suspend fun getUsersByRole(role: String): List<UserEntity> = withContext(Dispatchers.IO) {
        database.userDao().getUsersByRole(role)
    }

    suspend fun registerOrUpdateUser(user: UserEntity) = withContext(Dispatchers.IO) {
        database.userDao().insertUser(user)
    }

    suspend fun getWorkerById(workerId: String): WorkerProfileEntity? = withContext(Dispatchers.IO) {
        database.workerDao().getWorkerById(workerId)
    }

    suspend fun getWorkerByUserId(userId: String): WorkerProfileEntity? = withContext(Dispatchers.IO) {
        database.workerDao().getWorkerByUserId(userId)
    }

    fun getBookingsForCustomer(customerId: String): Flow<List<BookingEntity>> {
        return database.bookingDao().getBookingsByCustomer(customerId)
    }

    fun getBookingsForWorker(workerId: String): Flow<List<BookingEntity>> {
        return database.bookingDao().getBookingsByWorker(workerId)
    }

    fun getBookingsForCooperative(coopId: String): Flow<List<BookingEntity>> {
        return database.bookingDao().getBookingsByCooperative(coopId)
    }

    fun getBookingById(bookingId: String): Flow<BookingEntity?> {
        return database.bookingDao().getBookingFlowById(bookingId)
    }

    fun getWelfareWallet(workerId: String): Flow<WelfareWalletEntity?> {
        return database.welfareDao().getWalletForWorker(workerId)
    }

    fun getWelfareTransactions(workerId: String): Flow<List<WelfareTransactionEntity>> {
        return database.welfareDao().getTransactionsForWorker(workerId)
    }

    fun getRatingsForWorker(workerId: String): Flow<List<RatingEntity>> {
        return database.ratingDao().getRatingsForWorker(workerId)
    }

    suspend fun createBooking(
        customerId: String,
        customerName: String,
        customerPhone: String,
        workerId: String,
        workerName: String,
        cooperativeId: String,
        serviceCategory: String,
        problemDescription: String,
        urgency: String,
        isEmergency: Boolean,
        locationAddress: String,
        city: String,
        scheduledDate: String,
        scheduledTime: String,
        laborCost: Double
    ): String = withContext(Dispatchers.IO) {
        val bookingId = "bk_" + UUID.randomUUID().toString().replace("-", "").take(10)
        val bookingNumber = "BK-2026-" + (1000..9999).random()
        val emergencyFee = if (isEmergency) laborCost * 0.02 else 0.0
        val totalAmount = laborCost + 25.0 + emergencyFee
        val initialStatus = if (workerId.isNotBlank()) "ASSIGNED" else "PENDING"

        val booking = BookingEntity(
            id = bookingId,
            bookingNumber = bookingNumber,
            customerId = customerId,
            customerName = customerName,
            customerPhone = customerPhone,
            workerId = workerId,
            workerName = workerName,
            cooperativeId = cooperativeId,
            serviceCategory = serviceCategory,
            problemDescription = problemDescription,
            urgency = urgency,
            isEmergency = isEmergency,
            locationAddress = locationAddress,
            city = city,
            scheduledDate = scheduledDate,
            scheduledTime = scheduledTime,
            status = initialStatus,
            laborCost = laborCost,
            materialCost = 0.0,
            platformFee = 25.0,
            totalAmount = totalAmount,
            paymentStatus = "PENDING"
        )
        database.bookingDao().insertBooking(booking)

        // Save to Supabase remote database
        val supabaseBooking = SupabaseBooking(
            id = bookingId,
            bookingNumber = bookingNumber,
            customerId = customerId,
            customerName = customerName,
            customerPhone = customerPhone,
            workerId = workerId.ifBlank { null },
            workerName = workerName,
            cooperativeId = cooperativeId.ifBlank { null },
            serviceCategory = serviceCategory,
            problemDescription = problemDescription,
            urgency = urgency,
            isEmergency = isEmergency,
            locationAddress = locationAddress,
            city = city,
            scheduledDate = scheduledDate,
            scheduledTime = scheduledTime,
            status = initialStatus,
            laborCost = laborCost,
            materialCost = 0.0,
            platformFee = 25.0,
            totalAmount = totalAmount,
            paymentStatus = "PENDING",
            paymentMethod = "CASH_OR_UPI"
        )
        try {
            SupabaseService.createBooking(supabaseBooking)
            Log.i("SahakaarRepository", "Booking $bookingNumber saved to Supabase successfully")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("SahakaarRepository", "Supabase createBooking error: ${e.message}")
        }

        // Add notification for worker & customer
        database.notificationDao().insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = workerId,
                role = "WORKER",
                title = if (isEmergency) "🚨 Emergency Booking Received" else "New Job Assigned",
                message = "Booking #$bookingNumber from $customerName for $serviceCategory in $city."
            )
        )
        bookingId
    }

    suspend fun updateBookingStatus(bookingId: String, status: String) = withContext(Dispatchers.IO) {
        database.bookingDao().updateBookingStatus(bookingId, status)
        try {
            SupabaseService.updateBookingStatus(bookingId, status)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("SahakaarRepository", "Supabase updateBookingStatus error: ${e.message}")
        }
        if (status == "COMPLETED") {
            val booking = database.bookingDao().getBookingById(bookingId)
            if (booking != null) {
                database.workerDao().incrementCompletedJobs(booking.workerId)
                // Add 10% cooperative welfare contribution
                val welfareContribution = booking.laborCost * 0.10
                database.welfareDao().addBalance(booking.workerId, welfareContribution)
                database.welfareDao().insertTransaction(
                    WelfareTransactionEntity(
                        id = UUID.randomUUID().toString(),
                        workerId = booking.workerId,
                        amount = welfareContribution,
                        type = "COOP_CONTRIBUTION",
                        description = "Cooperative welfare credit from completed booking #${booking.bookingNumber}"
                    )
                )
            }
        }
    }

    suspend fun acceptJob(bookingId: String, workerId: String, workerName: String) = withContext(Dispatchers.IO) {
        database.bookingDao().assignWorker(bookingId, workerId, workerName)
        try {
            SupabaseService.updateBookingStatus(bookingId, "ASSIGNED", workerId, workerName)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("SahakaarRepository", "Supabase acceptJob error: ${e.message}")
        }
    }

    suspend fun processDemoPayment(bookingId: String, method: String) = withContext(Dispatchers.IO) {
        val booking = database.bookingDao().getBookingById(bookingId) ?: return@withContext
        val paymentId = "pay_" + UUID.randomUUID().toString().take(8)
        val txnRef = "TXN-SS-" + System.currentTimeMillis().toString().takeLast(8)

        database.paymentDao().insertPayment(
            PaymentEntity(
                id = paymentId,
                bookingId = bookingId,
                transactionRef = txnRef,
                amount = booking.totalAmount,
                paymentMethod = method,
                status = "SUCCESS"
            )
        )

        database.bookingDao().updateBookingPayment(bookingId, "PAID", method)
        try {
            SupabaseService.updateBookingPayment(bookingId, "PAID", method)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("SahakaarRepository", "Supabase updateBookingPayment error: ${e.message}")
        }

        // Generate digital invoice
        val invoiceId = "inv_" + UUID.randomUUID().toString().take(8)
        val invoice = InvoiceEntity(
            id = invoiceId,
            invoiceNumber = "INV-SS-" + (1000..9999).random(),
            bookingId = bookingId,
            customerName = booking.customerName,
            workerName = booking.workerName,
            cooperativeName = "Labour Cooperative Affiliated Partner",
            serviceName = booking.serviceCategory,
            laborCost = booking.laborCost,
            materialCost = booking.materialCost,
            platformFee = booking.platformFee,
            totalAmount = booking.totalAmount,
            paymentStatus = "PAID ($method)",
            paymentMethod = method,
            issuedDate = "02 Sep 2026"
        )
        database.invoiceDao().insertInvoice(invoice)
    }

    suspend fun cancelBooking(bookingId: String, reason: String = "Cancelled by user") = withContext(Dispatchers.IO) {
        database.bookingDao().updateBookingStatus(bookingId, "CANCELLED")
        try {
            SupabaseService.updateBookingStatus(bookingId, "CANCELLED")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("SahakaarRepository", "Supabase cancelBooking error: ${e.message}")
        }
        val booking = database.bookingDao().getBookingById(bookingId)
        if (booking != null && booking.workerId.isNotBlank()) {
            database.notificationDao().insertNotification(
                NotificationEntity(
                    id = "notif_" + UUID.randomUUID().toString().take(8),
                    userId = booking.workerId,
                    role = "WORKER",
                    title = "Booking Cancelled",
                    message = "Booking #${booking.bookingNumber} was cancelled. Reason: $reason",
                    isRead = false,
                    actionType = "BOOKING_UPDATE"
                )
            )
        }
        database.auditLogDao().insertAuditLog(
            AuditLogEntity(
                id = "aud_" + UUID.randomUUID().toString().take(8),
                actorUserId = booking?.customerId ?: "customer",
                action = "BOOKING_CANCELLED",
                targetUserId = booking?.workerId,
                metadata = "Booking #$bookingId cancelled with reason: $reason"
            )
        )
    }

    suspend fun submitRating(bookingId: String, workerId: String, customerId: String, customerName: String, rating: Int, review: String) = withContext(Dispatchers.IO) {
        val ratingId = "rat_" + UUID.randomUUID().toString().take(8)
        database.ratingDao().insertRating(
            RatingEntity(
                id = ratingId,
                bookingId = bookingId,
                workerId = workerId,
                customerId = customerId,
                customerName = customerName,
                rating = rating,
                reviewText = review
            )
        )
        // Recalculate average rating & rating count for the worker
        val worker = database.workerDao().getWorkerById(workerId)
        if (worker != null) {
            val totalScore = (worker.customerRating * worker.ratingCount) + rating.toDouble()
            val newCount = worker.ratingCount + 1
            val newAvg = (totalScore / newCount).let { Math.round(it * 10.0) / 10.0 }
            database.workerDao().updateWorkerRating(workerId, newAvg, newCount)
        }
        database.auditLogDao().insertAuditLog(
            AuditLogEntity(
                id = "aud_" + UUID.randomUUID().toString().take(8),
                actorUserId = customerId,
                action = "WORKER_RATING_SUBMITTED",
                targetUserId = workerId,
                metadata = "Customer $customerName submitted $rating-star rating for worker $workerId: $review"
            )
        )
    }

    suspend fun raiseDispute(bookingId: String, customerId: String, customerName: String, workerId: String, reason: String, description: String) = withContext(Dispatchers.IO) {
        val disputeId = "dsp_" + UUID.randomUUID().toString().take(8)
        database.disputeDao().insertDispute(
            DisputeEntity(
                id = disputeId,
                bookingId = bookingId,
                customerId = customerId,
                customerName = customerName,
                workerId = workerId,
                reason = reason,
                description = description,
                status = "OPEN"
            )
        )
    }

    suspend fun submitInstitutionalRequest(
        institutionName: String,
        institutionType: String,
        requiredSkill: String,
        workerCount: Int,
        durationDays: Int,
        locationCity: String,
        contactPerson: String,
        contactPhone: String,
        cooperativeId: String
    ) = withContext(Dispatchers.IO) {
        val estimatedRate = 450.0
        val budget = workerCount * durationDays * estimatedRate
        val instId = "inst_" + UUID.randomUUID().toString().take(8)
        database.institutionalBookingDao().insertInstitutionalBooking(
            InstitutionalBookingEntity(
                id = instId,
                institutionName = institutionName,
                institutionType = institutionType,
                requiredSkill = requiredSkill,
                workerCount = workerCount,
                durationDays = durationDays,
                locationCity = locationCity,
                contactPerson = contactPerson,
                contactPhone = contactPhone,
                status = "PENDING_ALLOCATION",
                cooperativeId = cooperativeId,
                estimatedBudget = budget
            )
        )
    }

    suspend fun setWorkerAvailability(workerId: String, isAvailable: Boolean) = withContext(Dispatchers.IO) {
        database.workerDao().updateWorkerAvailability(workerId, isAvailable)
    }

    suspend fun setWorkerVerification(workerId: String, status: String) = withContext(Dispatchers.IO) {
        database.workerDao().updateWorkerVerificationStatus(workerId, status)
    }

    suspend fun registerWorker(worker: WorkerProfileEntity) = withContext(Dispatchers.IO) {
        database.workerDao().insertWorker(worker)
        database.welfareDao().insertWallet(
            WelfareWalletEntity(
                workerId = worker.id,
                balance = 1000.0,
                insuranceCovered = "PM Suraksha Bima Yojana - ₹2,00,000",
                trainingCredits = 50,
                emergencyAssistanceFund = 5000.0,
                totalContributions = 500.0
            )
        )
    }

    suspend fun getInvoiceByBooking(bookingId: String): InvoiceEntity? = withContext(Dispatchers.IO) {
        database.invoiceDao().getInvoiceByBooking(bookingId)
    }

    suspend fun getUserByCustomIdTag(tag: String): UserEntity? = withContext(Dispatchers.IO) {
        database.userDao().getUserByCustomIdTag(tag)
    }

    fun getNotificationsForUser(userId: String, role: String): Flow<List<NotificationEntity>> {
        return database.notificationDao().getNotificationsForRoleOrUser(role, userId)
    }

    suspend fun markNotificationRead(id: String) = withContext(Dispatchers.IO) {
        database.notificationDao().markAsRead(id)
    }

    suspend fun markAllNotificationsRead(userId: String, role: String) = withContext(Dispatchers.IO) {
        database.notificationDao().markAllAsRead(userId, role)
    }

    suspend fun setUserActiveStatus(userId: String, isActive: Boolean) = withContext(Dispatchers.IO) {
        database.userDao().updateUserActiveStatus(userId, isActive)
        recordAuditLog(
            actorUserId = "SUPER_ADMIN",
            action = if (isActive) "ACTIVATE_USER" else "DEACTIVATE_USER",
            targetUserId = userId,
            metadata = "Account active state toggled to $isActive"
        )
    }

    suspend fun updateUserProfile(
        userId: String,
        name: String,
        email: String,
        state: String,
        district: String,
        village: String
    ) = withContext(Dispatchers.IO) {
        database.userDao().updateUserProfile(userId, name, email, state, district, village)
        recordAuditLog(
            actorUserId = userId,
            action = "UPDATE_PROFILE",
            targetUserId = userId,
            metadata = "Updated profile details"
        )
    }

    fun getWorkerProfileFlowByUserId(userId: String): Flow<WorkerProfileEntity?> {
        return database.workerDao().getWorkerFlowByUserId(userId)
    }

    suspend fun updateWorkerProfessionalProfile(
        userId: String,
        name: String,
        email: String,
        phone: String,
        city: String,
        state: String,
        district: String,
        village: String,
        primarySkill: String,
        secondarySkills: String,
        experienceYears: Int,
        certifications: String,
        dailyWageRate: Double,
        isAvailable: Boolean,
        isEmergencyReady: Boolean,
        zone: String
    ) = withContext(Dispatchers.IO) {
        // Update user entity
        database.userDao().updateUserContactAndProfile(
            userId = userId,
            name = name,
            email = email,
            phone = phone,
            city = city,
            state = state,
            district = district,
            village = village
        )
        // Update worker entity
        database.workerDao().updateWorkerProfessionalDetails(
            userId = userId,
            name = name,
            phone = phone,
            primarySkill = primarySkill,
            secondarySkills = secondarySkills,
            experienceYears = experienceYears,
            certifications = certifications,
            dailyWageRate = dailyWageRate,
            isAvailable = isAvailable,
            isEmergencyReady = isEmergencyReady,
            city = city,
            zone = zone
        )
        recordAuditLog(
            actorUserId = userId,
            action = "UPDATE_WORKER_PROFILE",
            targetUserId = userId,
            metadata = "Updated professional skills ($primarySkill), credentials, wage rate ($dailyWageRate) and contact ($phone)"
        )
    }

    suspend fun updateCustomerProfile(
        userId: String,
        name: String,
        email: String,
        phone: String,
        city: String,
        state: String,
        district: String,
        village: String
    ) = withContext(Dispatchers.IO) {
        database.userDao().updateUserContactAndProfile(
            userId = userId,
            name = name,
            email = email,
            phone = phone,
            city = city,
            state = state,
            district = district,
            village = village
        )
        recordAuditLog(
            actorUserId = userId,
            action = "UPDATE_CUSTOMER_PROFILE",
            targetUserId = userId,
            metadata = "Updated customer contact ($phone) and address ($district, $state)"
        )
    }

    suspend fun changePassword(
        userId: String,
        oldPlain: String,
        newPlain: String
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val user = database.userDao().getUserById(userId)
            ?: return@withContext Pair(false, "User not found.")

        if (user.passwordHash.isNotBlank() && !com.example.data.auth.PasswordSecurity.verifyPassword(oldPlain, user.passwordHash)) {
            return@withContext Pair(false, "Current password is incorrect.")
        }

        val validation = com.example.data.auth.PasswordSecurity.validatePasswordStrength(newPlain)
        if (!validation.first) {
            return@withContext Pair(false, validation.second)
        }

        val newHash = com.example.data.auth.PasswordSecurity.hashPassword(newPlain)
        database.userDao().updateUserPassword(userId, newHash)
        recordAuditLog(
            actorUserId = userId,
            action = "CHANGE_PASSWORD",
            targetUserId = userId,
            metadata = "User successfully updated password"
        )
        Pair(true, "Password updated successfully.")
    }

    suspend fun registerCustomer(
        name: String,
        mobile: String,
        email: String?,
        passwordPlain: String,
        state: String,
        district: String,
        village: String
    ): UserEntity = withContext(Dispatchers.IO) {
        val customerTag = "SS-CUS-" + (100000..999999).random()
        val passwordHash = com.example.data.auth.PasswordSecurity.hashPassword(passwordPlain)
        val validEmail = if (!email.isNullOrBlank() && email.contains("@")) {
            email.trim()
        } else {
            val digits = mobile.filter { it.isDigit() }.takeLast(8)
            "citizen.$digits@sahakaarsetu.in"
        }

        // STEP 1: Supabase Auth signUp
        var authUserId: String? = null
        try {
            val authUser = SupabaseService.signUp(
                email = validEmail,
                password = passwordPlain,
                metadata = mapOf(
                    "name" to name.trim(),
                    "phone" to mobile.trim(),
                    "role" to "CUSTOMER"
                )
            )
            authUserId = authUser.id
            Log.d("SahakaarRepository", "Supabase Auth signUp succeeded. User UUID: $authUserId")
        } catch (e: SupabaseException) {
            Log.w("SahakaarRepository", "Supabase signUp notice (${e.statusCode}): ${e.message}")
            // 1. Try to sign in if user was already created previously
            try {
                val existingAuth = SupabaseService.signInWithPassword(validEmail, passwordPlain)
                authUserId = existingAuth.id
                Log.d("SahakaarRepository", "Existing Supabase user retrieved via password: $authUserId")
            } catch (_: Exception) {
                // 2. Check if profile already exists in Supabase DB
                try {
                    val existingProf = SupabaseService.getProfileByEmail(validEmail)
                        ?: SupabaseService.getProfileByPhone(mobile.trim())
                        ?: SupabaseService.getProfileByPhone(mobile.filter { it.isDigit() })
                    authUserId = existingProf?.id
                } catch (_: Exception) {}
            }

            // 3. If Auth email service rate-limited (429: over_email_send_rate_limit) or network issue, generate robust ID
            if (authUserId == null) {
                val localExisting = database.userDao().getUserByPhone(mobile.trim())
                    ?: database.userDao().getUserByEmail(validEmail)
                authUserId = localExisting?.id ?: ("u_cust_" + UUID.randomUUID().toString().replace("-", "").take(12))
                Log.i("SahakaarRepository", "Supabase Auth rate-limited (${e.statusCode}). Proceeding with direct profile registration: $authUserId")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("SahakaarRepository", "Registration network notice: ${e.localizedMessage}")
            val localExisting = database.userDao().getUserByPhone(mobile.trim())
                ?: database.userDao().getUserByEmail(validEmail)
            authUserId = localExisting?.id ?: ("u_cust_" + UUID.randomUUID().toString().replace("-", "").take(12))
        }

        val finalUserId = authUserId ?: ("u_cust_" + UUID.randomUUID().toString().replace("-", "").take(12))

        // STEP 2: Real Supabase Profile row creation with Authenticated User UUID
        val supabaseProfile = SupabaseProfile(
            id = finalUserId, // Source of truth: auth.users.id UUID or resilient unique ID
            email = validEmail,
            name = name.trim(),
            role = "CUSTOMER",
            phone = mobile.trim(),
            city = district.trim(),
            state = state.trim().ifBlank { "Bihar" },
            district = district.trim().ifBlank { "Patna" },
            village = village.trim(),
            customIdTag = customerTag,
            isMobileVerified = true,
            isActive = true
        )

        try {
            val persistedProfile = SupabaseService.upsertProfile(supabaseProfile)
            Log.d("SahakaarRepository", "Supabase Profile successfully saved to database: ${persistedProfile.id}")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("SahakaarRepository", "Supabase profile sync warning: ${e.localizedMessage}. Profile stored locally.")
        }

        // STEP 3: Persist to local Room database for offline cache
        val user = UserEntity(
            id = finalUserId,
            email = validEmail,
            name = name.trim(),
            role = "CUSTOMER",
            phone = mobile.trim(),
            city = district.trim(),
            cooperativeId = null,
            customIdTag = customerTag,
            state = state.trim().ifBlank { "Bihar" },
            district = district.trim().ifBlank { "Patna" },
            village = village.trim(),
            passwordHash = passwordHash,
            isMobileVerified = true,
            isActive = true
        )
        database.userDao().insertUser(user)

        // Welcome notification
        database.notificationDao().insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = finalUserId,
                role = "CUSTOMER",
                title = "Welcome to Sahakaar Setu!",
                message = "Your verified Citizen ID is $customerTag. Find verified skilled cooperative artisans without commission.",
                isRead = false,
                actionType = "WELCOME"
            )
        )

        recordAuditLog(
            actorUserId = finalUserId,
            action = "REGISTER_CUSTOMER",
            targetUserId = finalUserId,
            metadata = "New customer registered with tag $customerTag in Supabase database"
        )
        user
    }

    suspend fun registerWorkerFull(
        name: String,
        mobile: String,
        email: String?,
        passwordPlain: String,
        state: String,
        district: String,
        village: String,
        profession: String,
        skills: List<String>,
        experienceYears: Int,
        dailyRate: Double,
        cooperativeId: String = "coop_patna"
    ): Pair<UserEntity, WorkerProfileEntity> = withContext(Dispatchers.IO) {
        val workerTag = "SS-WRK-" + (100000..999999).random()
        val passwordHash = com.example.data.auth.PasswordSecurity.hashPassword(passwordPlain)
        val validEmail = if (!email.isNullOrBlank() && email.contains("@")) {
            email.trim()
        } else {
            val digits = mobile.filter { it.isDigit() }.takeLast(8)
            "artisan.$digits@sahakaarsetu.in"
        }

        // STEP 1: Supabase Auth signUp
        var authUserId: String? = null
        try {
            val authUser = SupabaseService.signUp(
                email = validEmail,
                password = passwordPlain,
                metadata = mapOf(
                    "name" to name.trim(),
                    "phone" to mobile.trim(),
                    "role" to "WORKER",
                    "profession" to profession
                )
            )
            authUserId = authUser.id
            Log.d("SahakaarRepository", "Supabase Worker Auth signUp succeeded. UUID: $authUserId")
        } catch (e: SupabaseException) {
            Log.w("SahakaarRepository", "Supabase Worker Auth signUp notice (${e.statusCode}): ${e.message}")
            try {
                val existingAuth = SupabaseService.signInWithPassword(validEmail, passwordPlain)
                authUserId = existingAuth.id
                Log.d("SahakaarRepository", "Retrieved existing artisan Auth user: $authUserId")
            } catch (_: Exception) {
                try {
                    val existingProf = SupabaseService.getProfileByEmail(validEmail)
                        ?: SupabaseService.getProfileByPhone(mobile.trim())
                        ?: SupabaseService.getProfileByPhone(mobile.filter { it.isDigit() })
                    authUserId = existingProf?.id
                } catch (_: Exception) {}
            }

            if (authUserId == null) {
                val localExisting = database.userDao().getUserByPhone(mobile.trim())
                    ?: database.userDao().getUserByEmail(validEmail)
                authUserId = localExisting?.id ?: ("u_work_" + UUID.randomUUID().toString().replace("-", "").take(12))
                Log.i("SahakaarRepository", "Supabase Auth rate-limited (${e.statusCode}). Proceeding with direct worker registration: $authUserId")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("SahakaarRepository", "Artisan registration network notice: ${e.localizedMessage}")
            val localExisting = database.userDao().getUserByPhone(mobile.trim())
                ?: database.userDao().getUserByEmail(validEmail)
            authUserId = localExisting?.id ?: ("u_work_" + UUID.randomUUID().toString().replace("-", "").take(12))
        }

        val finalUserId = authUserId ?: ("u_work_" + UUID.randomUUID().toString().replace("-", "").take(12))
        val workerId = "w_" + finalUserId.take(12)

        // STEP 2: Supabase Profile insertion
        val supabaseProfile = SupabaseProfile(
            id = finalUserId,
            email = validEmail,
            name = name.trim(),
            role = "WORKER",
            phone = mobile.trim(),
            city = district.trim(),
            state = state.trim().ifBlank { "Bihar" },
            district = district.trim().ifBlank { "Patna" },
            village = village.trim(),
            cooperativeId = cooperativeId,
            customIdTag = workerTag,
            isMobileVerified = true,
            isActive = true
        )
        try {
            SupabaseService.upsertProfile(supabaseProfile)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("SahakaarRepository", "Artisan profile remote sync notice: ${e.localizedMessage}")
        }

        val coopName = database.cooperativeDao().getCooperativeById(cooperativeId)?.name
            ?: "Patna Shramik Vikas Sahakari Samiti"

        // STEP 3: Supabase Worker record insertion
        val supabaseWorker = SupabaseWorker(
            id = workerId,
            userId = finalUserId,
            workerIdTag = workerTag,
            name = name.trim(),
            phone = mobile.trim(),
            cooperativeId = cooperativeId,
            cooperativeName = coopName,
            primarySkill = profession,
            secondarySkills = skills.joinToString(", ").ifBlank { profession },
            experienceYears = experienceYears,
            verificationStatus = "VERIFIED",
            certifications = "State Cooperative Certified Artisan (Skill Verified)",
            customerRating = 5.0,
            ratingCount = 1,
            completedJobs = 0,
            reliabilityScore = 100,
            dailyWageRate = dailyRate,
            isAvailable = true,
            isBusy = false,
            isEmergencyReady = true,
            lat = 25.5941 + java.util.concurrent.ThreadLocalRandom.current().nextDouble(-0.03, 0.03),
            lng = 85.1376 + java.util.concurrent.ThreadLocalRandom.current().nextDouble(-0.03, 0.03),
            city = district.trim(),
            zone = village.ifBlank { "$district Central" }
        )

        try {
            SupabaseService.insertWorker(supabaseWorker)
            Log.d("SahakaarRepository", "Artisan record saved to Supabase workers table: $workerId")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("SahakaarRepository", "Warning: worker row insert to Supabase: ${e.localizedMessage}")
        }

        // STEP 4: Local Room cache
        val user = UserEntity(
            id = finalUserId,
            email = validEmail,
            name = name.trim(),
            role = "WORKER",
            phone = mobile.trim(),
            city = district.trim(),
            cooperativeId = cooperativeId,
            customIdTag = workerTag,
            state = state.trim().ifBlank { "Bihar" },
            district = district.trim().ifBlank { "Patna" },
            village = village.trim(),
            passwordHash = passwordHash,
            isMobileVerified = true,
            isActive = true
        )
        database.userDao().insertUser(user)

        val workerProfile = WorkerProfileEntity(
            id = workerId,
            userId = finalUserId,
            workerIdTag = workerTag,
            name = name.trim(),
            phone = mobile.trim(),
            cooperativeId = cooperativeId,
            cooperativeName = coopName,
            primarySkill = profession,
            secondarySkills = skills.joinToString(", "),
            experienceYears = experienceYears,
            verificationStatus = "VERIFIED",
            certifications = "State Cooperative Certified Artisan (Skill Verified)",
            customerRating = 5.0f,
            ratingCount = 1,
            completedJobs = 0,
            reliabilityScore = 95,
            dailyWageRate = dailyRate,
            isAvailable = true,
            isBusy = false,
            isEmergencyReady = true,
            lat = supabaseWorker.lat,
            lng = supabaseWorker.lng,
            city = district.trim(),
            zone = village.ifBlank { "$district Central" }
        )
        registerWorker(workerProfile)

        // Welcome notification
        database.notificationDao().insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = finalUserId,
                role = "WORKER",
                title = "Worker ID Issued: $workerTag",
                message = "Welcome to Sahakaar Setu Labour Cooperative network! Welfare wallet credited with ₹1000 starter pool.",
                isRead = false,
                actionType = "WALLET_CREDIT"
            )
        )

        recordAuditLog(
            actorUserId = finalUserId,
            action = "REGISTER_WORKER",
            targetUserId = finalUserId,
            metadata = "New worker registered with tag $workerTag ($profession)"
        )
        Pair(user, workerProfile)
    }

    suspend fun loginWithSupabase(identifier: String, passwordPlain: String): Pair<UserEntity, String> = withContext(Dispatchers.IO) {
        val trimmed = identifier.trim()
        val isEmail = trimmed.contains("@")
        var authUser: SupabaseAuthUser? = null
        var supabaseProfile: SupabaseProfile? = null

        if (isEmail) {
            try {
                authUser = SupabaseService.signInWithPassword(trimmed, passwordPlain)
            } catch (e: SupabaseException) {
                Log.w("SahakaarRepository", "Supabase signInWithPassword notice (${e.statusCode}): ${e.message}")
                try {
                    supabaseProfile = SupabaseService.getProfileByEmail(trimmed)
                } catch (_: Exception) {}

                if (supabaseProfile == null) {
                    val localUser = database.userDao().getUserByEmail(trimmed)
                    if (localUser != null) {
                        return@withContext Pair(localUser, localUser.role)
                    }
                    if (e.statusCode != 429 && e.errorCode != "email_not_confirmed") {
                        throw e
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("SahakaarRepository", "Supabase Auth login connection notice: ${e.localizedMessage}")
                try {
                    supabaseProfile = SupabaseService.getProfileByEmail(trimmed)
                } catch (ce: CancellationException) {
                    throw ce
                } catch (_: Exception) {}

                if (supabaseProfile == null) {
                    val localUser = database.userDao().getUserByEmail(trimmed)
                    if (localUser != null) {
                        return@withContext Pair(localUser, localUser.role)
                    }
                    throw e
                }
            }
        } else {
            val cleanPhone = trimmed.filter { it.isDigit() }
            try {
                supabaseProfile = SupabaseService.getProfileByPhone(cleanPhone)
                    ?: SupabaseService.getProfileByPhone(trimmed)
                    ?: SupabaseService.getProfileByPhone("+91 $cleanPhone")
                    ?: SupabaseService.getProfileByPhone("+91$cleanPhone")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("SahakaarRepository", "Supabase getProfileByPhone notice: ${e.localizedMessage}")
            }

            if (supabaseProfile == null && trimmed.startsWith("SS-", ignoreCase = true)) {
                try {
                    val local = database.userDao().getUserByCustomIdTag(trimmed)
                    if (local != null) return@withContext Pair(local, local.role)
                } catch (_: Exception) {}
            }
        }

        val targetUserId = authUser?.id ?: supabaseProfile?.id

        if (targetUserId != null && supabaseProfile == null) {
            try {
                supabaseProfile = SupabaseService.getProfile(targetUserId)
            } catch (_: Exception) {}
        }

        if (authUser != null && supabaseProfile == null) {
            // Profile does not exist yet: create it in Supabase using the authenticated user UUID!
            val newProfile = SupabaseProfile(
                id = authUser.id,
                email = authUser.email ?: trimmed,
                name = "Citizen ${authUser.email?.substringBefore('@') ?: "User"}",
                role = "CUSTOMER",
                phone = authUser.phone ?: "",
                city = "Patna",
                state = "Bihar",
                district = "Patna",
                customIdTag = "SS-CUS-" + (100000..999999).random()
            )
            try {
                supabaseProfile = SupabaseService.upsertProfile(newProfile)
            } catch (_: Exception) {}
        }

        if (supabaseProfile != null) {
            val user = UserEntity(
                id = supabaseProfile.id,
                email = supabaseProfile.email,
                name = supabaseProfile.name,
                role = supabaseProfile.role,
                phone = supabaseProfile.phone,
                city = supabaseProfile.city,
                state = supabaseProfile.state,
                district = supabaseProfile.district,
                village = supabaseProfile.village,
                cooperativeId = supabaseProfile.cooperativeId,
                customIdTag = supabaseProfile.customIdTag,
                isMobileVerified = supabaseProfile.isMobileVerified,
                isActive = supabaseProfile.isActive
            )
            database.userDao().insertUser(user)
            return@withContext Pair(user, user.role)
        }

        // Local fallback
        val cleanPhone = trimmed.filter { it.isDigit() }
        val localUser = database.userDao().getUserByEmail(trimmed)
            ?: database.userDao().getUserByPhone(trimmed)
            ?: (if (cleanPhone.isNotBlank()) database.userDao().getUserByPhone(cleanPhone) else null)
            ?: (if (cleanPhone.isNotBlank()) database.userDao().getUserByPhone("+91 $cleanPhone") else null)
            ?: database.userDao().getUserByCustomIdTag(trimmed)
            ?: DatabaseSeeder.getInitialUsers().find {
                it.email.equals(trimmed, ignoreCase = true) ||
                it.phone.contains(trimmed) ||
                it.customIdTag.equals(trimmed, ignoreCase = true)
            }
            ?: throw Exception("Account not found for '$trimmed'. Please check credentials or create a new account.")
        Pair(localUser, localUser.role)
    }

    suspend fun updateUserProfileInSupabase(
        userId: String,
        name: String,
        phone: String,
        city: String,
        district: String,
        village: String
    ): UserEntity = withContext(Dispatchers.IO) {
        val updates = mapOf(
            "name" to name.trim(),
            "phone" to phone.trim(),
            "city" to city.trim(),
            "district" to district.trim(),
            "village" to village.trim()
        )
        val updated = SupabaseService.updateProfile(userId, updates)
        val entity = UserEntity(
            id = updated.id,
            email = updated.email,
            name = updated.name,
            role = updated.role,
            phone = updated.phone,
            city = updated.city,
            state = updated.state,
            district = updated.district,
            village = updated.village,
            cooperativeId = updated.cooperativeId,
            customIdTag = updated.customIdTag,
            isMobileVerified = updated.isMobileVerified,
            isActive = updated.isActive
        )
        database.userDao().insertUser(entity)
        entity
    }

    suspend fun createBookingInSupabase(
        customerId: String,
        customerName: String,
        customerPhone: String,
        workerId: String?,
        workerName: String,
        cooperativeId: String?,
        serviceCategory: String,
        problemDescription: String,
        address: String,
        city: String,
        date: String,
        time: String,
        laborCost: Double
    ): SupabaseBooking = withContext(Dispatchers.IO) {
        val bookingNumber = "BK-" + System.currentTimeMillis().toString().takeLast(6)
        val booking = SupabaseBooking(
            id = UUID.randomUUID().toString(),
            bookingNumber = bookingNumber,
            customerId = customerId,
            customerName = customerName,
            customerPhone = customerPhone,
            workerId = workerId,
            workerName = workerName,
            cooperativeId = cooperativeId,
            serviceCategory = serviceCategory,
            problemDescription = problemDescription,
            urgency = "NORMAL",
            isEmergency = false,
            locationAddress = address,
            city = city,
            scheduledDate = date,
            scheduledTime = time,
            status = "CONFIRMED",
            laborCost = laborCost,
            materialCost = 0.0,
            platformFee = 0.0,
            totalAmount = laborCost,
            paymentStatus = "PENDING_CASH",
            paymentMethod = "CASH_ON_COMPLETION"
        )
        val saved = SupabaseService.createBooking(booking)

        // Also save to local Room
        val entity = BookingEntity(
            id = saved.id,
            bookingNumber = saved.bookingNumber,
            customerId = saved.customerId,
            customerName = saved.customerName,
            customerPhone = saved.customerPhone,
            workerId = saved.workerId ?: "w_pending",
            workerName = saved.workerName ?: "Assigned Cooperative Artisan",
            cooperativeId = saved.cooperativeId ?: "coop_patna",
            serviceCategory = saved.serviceCategory,
            problemDescription = saved.problemDescription,
            urgency = "NORMAL",
            isEmergency = false,
            locationAddress = saved.locationAddress,
            city = saved.city,
            scheduledDate = saved.scheduledDate,
            scheduledTime = saved.scheduledTime,
            status = "CONFIRMED",
            laborCost = saved.laborCost,
            materialCost = saved.materialCost,
            platformFee = saved.platformFee,
            totalAmount = saved.totalAmount,
            paymentStatus = saved.paymentStatus,
            paymentMethod = saved.paymentMethod
        )
        database.bookingDao().insertBooking(entity)
        saved
    }

    suspend fun syncLiveWorkersFromSupabase(): List<WorkerProfileEntity> = withContext(Dispatchers.IO) {
        try {
            val liveWorkers = SupabaseService.getWorkers()
            if (liveWorkers.isNotEmpty()) {
                val entities = liveWorkers.map { sw ->
                    WorkerProfileEntity(
                        id = sw.id,
                        userId = sw.userId ?: ("u_" + sw.id),
                        workerIdTag = sw.workerIdTag,
                        name = sw.name,
                        phone = sw.phone,
                        cooperativeId = sw.cooperativeId ?: "coop_patna",
                        cooperativeName = sw.cooperativeName ?: "Patna Shramik Vikas Sahakari Samiti",
                        primarySkill = sw.primarySkill,
                        secondarySkills = sw.secondarySkills ?: sw.primarySkill,
                        experienceYears = sw.experienceYears,
                        verificationStatus = sw.verificationStatus,
                        certifications = sw.certifications ?: "Verified Cooperative Artisan",
                        customerRating = sw.customerRating.toFloat(),
                        ratingCount = sw.ratingCount,
                        completedJobs = sw.completedJobs,
                        reliabilityScore = sw.reliabilityScore,
                        dailyWageRate = sw.dailyWageRate,
                        isAvailable = sw.isAvailable,
                        isBusy = sw.isBusy,
                        isEmergencyReady = sw.isEmergencyReady,
                        lat = sw.lat,
                        lng = sw.lng,
                        city = sw.city,
                        zone = sw.zone,
                        photoAvatarId = 1
                    )
                }
                database.workerDao().insertWorkers(entities)
                return@withContext entities
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("SahakaarRepository", "Supabase sync error: ${e.localizedMessage}")
        }
        database.workerDao().getAllWorkers().first()
    }

    /**
     * Strict 10 KM Nearby Rule enforcement (Haversine formula).
     * Filters workers strictly within maxRadiusKm (10.0 km default).
     */
    fun filterNearbyWorkers(
        workers: List<WorkerProfileEntity>,
        userLat: Double,
        userLng: Double,
        maxRadiusKm: Double = 10.0
    ): List<WorkerProfileEntity> {
        return workers.filter { worker ->
            val dist = calculateHaversineKm(userLat, userLng, worker.lat, worker.lng)
            dist <= maxRadiusKm
        }.sortedBy { worker ->
            calculateHaversineKm(userLat, userLng, worker.lat, worker.lng)
        }
    }

    private fun calculateHaversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth's radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    suspend fun recordAuditLog(
        actorUserId: String,
        action: String,
        targetUserId: String? = null,
        metadata: String = ""
    ) = withContext(Dispatchers.IO) {
        database.auditLogDao().insertAuditLog(
            AuditLogEntity(
                id = "log_" + UUID.randomUUID().toString().take(8),
                actorUserId = actorUserId,
                action = action,
                targetUserId = targetUserId,
                metadata = metadata
            )
        )
    }

    suspend fun markNotificationAsRead(id: String) = withContext(Dispatchers.IO) {
        database.notificationDao().markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead(userId: String, role: String) = withContext(Dispatchers.IO) {
        database.notificationDao().markAllAsRead(userId, role)
    }

    suspend fun syncBookingsFromSupabase(): List<BookingEntity> = withContext(Dispatchers.IO) {
        try {
            val remoteBookings = SupabaseService.getAllBookings()
            if (remoteBookings.isNotEmpty()) {
                val entities = remoteBookings.map { sb ->
                    BookingEntity(
                        id = sb.id,
                        bookingNumber = sb.bookingNumber,
                        customerId = sb.customerId,
                        customerName = sb.customerName,
                        customerPhone = sb.customerPhone,
                        workerId = sb.workerId ?: "",
                        workerName = sb.workerName,
                        cooperativeId = sb.cooperativeId ?: "coop_patna",
                        serviceCategory = sb.serviceCategory,
                        problemDescription = sb.problemDescription,
                        urgency = sb.urgency,
                        isEmergency = sb.isEmergency,
                        locationAddress = sb.locationAddress,
                        city = sb.city,
                        scheduledDate = sb.scheduledDate,
                        scheduledTime = sb.scheduledTime,
                        status = sb.status,
                        laborCost = sb.laborCost,
                        materialCost = sb.materialCost,
                        platformFee = sb.platformFee,
                        totalAmount = sb.totalAmount,
                        paymentStatus = sb.paymentStatus
                    )
                }
                database.bookingDao().insertBookings(entities)
                return@withContext entities
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("SahakaarRepository", "syncBookingsFromSupabase failed: ${e.message}")
        }
        database.bookingDao().getAllBookings().first()
    }

    suspend fun addProfession(nameEn: String, nameHi: String) = withContext(Dispatchers.IO) {
        database.professionDao().insertProfession(
            ProfessionEntity(
                id = "prof_" + UUID.randomUUID().toString().take(6),
                nameEn = nameEn,
                nameHi = nameHi,
                iconName = "ic_skill",
                isActive = true
            )
        )
    }

    fun getAllLocations(): Flow<List<LocationEntity>> =
        database.locationDao().getAllLocations()

    fun getLocationsByDistrict(district: String): Flow<List<LocationEntity>> =
        database.locationDao().getLocationsByDistrict(district)

    fun getDistinctDistricts(): Flow<List<String>> =
        database.locationDao().getDistinctDistricts()
}
