package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = :role")
    suspend fun getUsersByRole(role: String): List<UserEntity>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE customIdTag = :tag LIMIT 1")
    suspend fun getUserByCustomIdTag(tag: String): UserEntity?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Query("UPDATE users SET isActive = :isActive WHERE id = :userId")
    suspend fun updateUserActiveStatus(userId: String, isActive: Boolean)

    @Query("UPDATE users SET passwordHash = :passwordHash WHERE id = :userId")
    suspend fun updateUserPassword(userId: String, passwordHash: String)

    @Query("UPDATE users SET name = :name, email = :email, state = :state, district = :district, village = :village WHERE id = :userId")
    suspend fun updateUserProfile(userId: String, name: String, email: String, state: String, district: String, village: String)

    @Query("UPDATE users SET name = :name, email = :email, phone = :phone, city = :city, state = :state, district = :district, village = :village WHERE id = :userId")
    suspend fun updateUserContactAndProfile(
        userId: String,
        name: String,
        email: String,
        phone: String,
        city: String,
        state: String,
        district: String,
        village: String
    )
}

@Dao
interface CooperativeDao {
    @Query("SELECT * FROM cooperatives")
    fun getAllCooperatives(): Flow<List<CooperativeEntity>>

    @Query("SELECT * FROM cooperatives WHERE id = :id LIMIT 1")
    suspend fun getCooperativeById(id: String): CooperativeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCooperatives(cooperatives: List<CooperativeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCooperative(cooperative: CooperativeEntity)
}

@Dao
interface WorkerDao {
    @Query("SELECT * FROM workers ORDER BY customerRating DESC")
    fun getAllWorkers(): Flow<List<WorkerProfileEntity>>

    @Query("SELECT * FROM workers WHERE cooperativeId = :cooperativeId")
    fun getWorkersByCooperative(cooperativeId: String): Flow<List<WorkerProfileEntity>>

    @Query("SELECT * FROM workers WHERE id = :id LIMIT 1")
    suspend fun getWorkerById(id: String): WorkerProfileEntity?

    @Query("SELECT * FROM workers WHERE userId = :userId LIMIT 1")
    suspend fun getWorkerByUserId(userId: String): WorkerProfileEntity?

    @Query("SELECT * FROM workers WHERE primarySkill = :skill AND isAvailable = 1")
    fun getAvailableWorkersBySkill(skill: String): Flow<List<WorkerProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: WorkerProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkers(workers: List<WorkerProfileEntity>)

    @Query("UPDATE workers SET isAvailable = :isAvailable WHERE id = :workerId")
    suspend fun updateWorkerAvailability(workerId: String, isAvailable: Boolean)

    @Query("UPDATE workers SET verificationStatus = :status WHERE id = :workerId")
    suspend fun updateWorkerVerificationStatus(workerId: String, status: String)

    @Query("UPDATE workers SET completedJobs = completedJobs + 1 WHERE id = :workerId")
    suspend fun incrementCompletedJobs(workerId: String)

    @Query("UPDATE workers SET customerRating = :rating, ratingCount = :ratingCount WHERE id = :workerId")
    suspend fun updateWorkerRating(workerId: String, rating: Double, ratingCount: Int)

    @Query("SELECT * FROM workers WHERE userId = :userId LIMIT 1")
    fun getWorkerFlowByUserId(userId: String): Flow<WorkerProfileEntity?>

    @Query("UPDATE workers SET name = :name, phone = :phone, primarySkill = :primarySkill, secondarySkills = :secondarySkills, experienceYears = :experienceYears, certifications = :certifications, dailyWageRate = :dailyWageRate, isAvailable = :isAvailable, isEmergencyReady = :isEmergencyReady, city = :city, zone = :zone WHERE userId = :userId")
    suspend fun updateWorkerProfessionalDetails(
        userId: String,
        name: String,
        phone: String,
        primarySkill: String,
        secondarySkills: String,
        experienceYears: Int,
        certifications: String,
        dailyWageRate: Double,
        isAvailable: Boolean,
        isEmergencyReady: Boolean,
        city: String,
        zone: String
    )
}

@Dao
interface ServiceCategoryDao {
    @Query("SELECT * FROM service_categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<ServiceCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<ServiceCategoryEntity>)
}

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY createdAt DESC")
    fun getAllBookings(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getBookingsByCustomer(customerId: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE workerId = :workerId ORDER BY createdAt DESC")
    fun getBookingsByWorker(workerId: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE cooperativeId = :coopId ORDER BY createdAt DESC")
    fun getBookingsByCooperative(coopId: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    suspend fun getBookingById(id: String): BookingEntity?

    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    fun getBookingFlowById(id: String): Flow<BookingEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookings(bookings: List<BookingEntity>)

    @Query("UPDATE bookings SET status = :status WHERE id = :bookingId")
    suspend fun updateBookingStatus(bookingId: String, status: String)

    @Query("UPDATE bookings SET paymentStatus = :paymentStatus, paymentMethod = :method WHERE id = :bookingId")
    suspend fun updateBookingPayment(bookingId: String, paymentStatus: String, method: String)

    @Query("UPDATE bookings SET workerId = :workerId, workerName = :workerName, status = 'ASSIGNED' WHERE id = :bookingId")
    suspend fun assignWorker(bookingId: String, workerId: String, workerName: String)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE bookingId = :bookingId LIMIT 1")
    suspend fun getPaymentByBooking(bookingId: String): PaymentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY id DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE bookingId = :bookingId LIMIT 1")
    suspend fun getInvoiceByBooking(bookingId: String): InvoiceEntity?

    @Query("SELECT * FROM invoices WHERE id = :invoiceId LIMIT 1")
    suspend fun getInvoiceById(invoiceId: String): InvoiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity)
}

@Dao
interface RatingDao {
    @Query("SELECT * FROM ratings WHERE workerId = :workerId ORDER BY timestamp DESC")
    fun getRatingsForWorker(workerId: String): Flow<List<RatingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRating(rating: RatingEntity)
}

@Dao
interface DisputeDao {
    @Query("SELECT * FROM disputes ORDER BY createdAt DESC")
    fun getAllDisputes(): Flow<List<DisputeEntity>>

    @Query("SELECT * FROM disputes WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getDisputesByCustomer(customerId: String): Flow<List<DisputeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDispute(dispute: DisputeEntity)

    @Query("UPDATE disputes SET status = :status, adminResponse = :adminResponse WHERE id = :disputeId")
    suspend fun resolveDispute(disputeId: String, status: String, adminResponse: String)
}

@Dao
interface WelfareDao {
    @Query("SELECT * FROM welfare_wallets WHERE workerId = :workerId LIMIT 1")
    fun getWalletForWorker(workerId: String): Flow<WelfareWalletEntity?>

    @Query("SELECT * FROM welfare_transactions WHERE workerId = :workerId ORDER BY timestamp DESC")
    fun getTransactionsForWorker(workerId: String): Flow<List<WelfareTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WelfareWalletEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallets(wallets: List<WelfareWalletEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WelfareTransactionEntity)

    @Query("UPDATE welfare_wallets SET balance = balance + :amount WHERE workerId = :workerId")
    suspend fun addBalance(workerId: String, amount: Double)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE role = :role OR userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsForRoleOrUser(role: String, userId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId OR role = :role")
    suspend fun markAllAsRead(userId: String, role: String)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY createdAt DESC")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)
}

@Dao
interface ProfessionDao {
    @Query("SELECT * FROM professions WHERE isActive = 1 ORDER BY nameEn ASC")
    fun getAllProfessions(): Flow<List<ProfessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfessions(professions: List<ProfessionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfession(profession: ProfessionEntity)
}

@Dao
interface DemandForecastDao {
    @Query("SELECT * FROM demand_forecasts")
    fun getAllForecasts(): Flow<List<DemandForecastEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecasts(forecasts: List<DemandForecastEntity>)
}

@Dao
interface InstitutionalBookingDao {
    @Query("SELECT * FROM institutional_bookings ORDER BY createdAt DESC")
    fun getAllInstitutionalBookings(): Flow<List<InstitutionalBookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstitutionalBooking(booking: InstitutionalBookingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstitutionalBookings(bookings: List<InstitutionalBookingEntity>)

    @Query("UPDATE institutional_bookings SET status = :status WHERE id = :id")
    suspend fun updateInstitutionalBookingStatus(id: String, status: String)
}

@Dao
interface LocationDao {
    @Query("SELECT * FROM locations ORDER BY district, cityOrTown ASC")
    fun getAllLocations(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM locations WHERE district = :district ORDER BY villageOrArea ASC")
    fun getLocationsByDistrict(district: String): Flow<List<LocationEntity>>

    @Query("SELECT DISTINCT district FROM locations ORDER BY district ASC")
    fun getDistinctDistricts(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<LocationEntity>)

    @Query("SELECT COUNT(*) FROM locations")
    suspend fun getLocationCount(): Int
}
