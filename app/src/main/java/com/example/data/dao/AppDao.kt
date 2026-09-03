package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CooperativeDao {
    @Query("SELECT * FROM cooperatives ORDER BY name ASC")
    fun getAllCooperatives(): Flow<List<CooperativeEntity>>

    @Query("SELECT * FROM cooperatives WHERE id = :id LIMIT 1")
    suspend fun getCooperativeById(id: Long): CooperativeEntity?

    @Query("SELECT COUNT(*) FROM cooperatives")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCooperatives(list: List<CooperativeEntity>)
}

@Dao
interface WorkerDao {
    @Query("SELECT * FROM workers ORDER BY rating DESC, completedJobs DESC")
    fun getAllWorkers(): Flow<List<WorkerProfileEntity>>

    @Query("SELECT * FROM workers WHERE cooperativeId = :coopId ORDER BY name ASC")
    fun getWorkersByCooperative(coopId: Long): Flow<List<WorkerProfileEntity>>

    @Query("SELECT * FROM workers WHERE id = :id LIMIT 1")
    fun getWorkerById(id: Long): Flow<WorkerProfileEntity?>

    @Query("SELECT * FROM workers WHERE id = :id LIMIT 1")
    suspend fun getWorkerByIdSync(id: Long): WorkerProfileEntity?

    @Query("SELECT * FROM workers WHERE primarySkill = :skill AND isVerified = 1 ORDER BY rating DESC")
    suspend fun getWorkersBySkillSync(skill: String): List<WorkerProfileEntity>

    @Query("SELECT * FROM workers WHERE isVerified = 1")
    suspend fun getAllVerifiedWorkersSync(): List<WorkerProfileEntity>

    @Query("SELECT COUNT(*) FROM workers")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: WorkerProfileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkers(workers: List<WorkerProfileEntity>)

    @Update
    suspend fun updateWorker(worker: WorkerProfileEntity)

    @Query("UPDATE workers SET isOnline = :isOnline WHERE id = :id")
    suspend fun setWorkerAvailability(id: Long, isOnline: Boolean)

    @Query("UPDATE workers SET isBusy = :isBusy WHERE id = :id")
    suspend fun setWorkerBusy(id: Long, isBusy: Boolean)

    @Query("UPDATE workers SET welfareBalance = welfareBalance + :amount WHERE id = :id")
    suspend fun addWelfareBalance(id: Long, amount: Double)

    @Query("UPDATE workers SET dailyEarnings = dailyEarnings + :amount, completedJobs = completedJobs + 1, isBusy = 0 WHERE id = :id")
    suspend fun completeJobForWorker(id: Long, amount: Double)
}

@Dao
interface ServiceDao {
    @Query("SELECT * FROM service_categories ORDER BY id ASC")
    fun getAllServices(): Flow<List<ServiceCategoryEntity>>

    @Query("SELECT COUNT(*) FROM service_categories")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(services: List<ServiceCategoryEntity>)
}

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY createdAt DESC")
    fun getAllBookings(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE workerId = :workerId ORDER BY createdAt DESC")
    fun getBookingsByWorker(workerId: Long): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getBookingsByCustomer(customerId: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE status = :status ORDER BY createdAt DESC")
    fun getBookingsByStatus(status: BookingStatus): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE isEmergency = 1 ORDER BY createdAt DESC")
    fun getEmergencyBookings(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    fun getBookingById(id: Long): Flow<BookingEntity?>

    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    suspend fun getBookingByIdSync(id: Long): BookingEntity?

    @Query("SELECT COUNT(*) FROM bookings")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookings(bookings: List<BookingEntity>)

    @Update
    suspend fun updateBooking(booking: BookingEntity)

    @Query("UPDATE bookings SET status = :status WHERE id = :id")
    suspend fun updateBookingStatus(id: Long, status: BookingStatus)

    @Query("UPDATE bookings SET paymentStatus = :paymentStatus, paymentMethod = :method, paymentTransactionId = :txId WHERE id = :id")
    suspend fun updatePayment(id: Long, paymentStatus: PaymentStatus, method: String, txId: String)
}

@Dao
interface WelfareDao {
    @Query("SELECT * FROM welfare_transactions WHERE workerId = :workerId ORDER BY timestamp DESC")
    fun getTransactionsForWorker(workerId: Long): Flow<List<WelfareTransactionEntity>>

    @Query("SELECT * FROM welfare_transactions ORDER BY timestamp DESC")
    fun getAllWelfareTransactions(): Flow<List<WelfareTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: WelfareTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(txs: List<WelfareTransactionEntity>)
}

@Dao
interface RatingDao {
    @Query("SELECT * FROM ratings WHERE workerId = :workerId ORDER BY timestamp DESC")
    fun getRatingsForWorker(workerId: Long): Flow<List<RatingEntity>>

    @Query("SELECT * FROM ratings ORDER BY timestamp DESC")
    fun getAllRatings(): Flow<List<RatingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRating(rating: RatingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRatings(ratings: List<RatingEntity>)
}

@Dao
interface DisputeDao {
    @Query("SELECT * FROM disputes ORDER BY createdAt DESC")
    fun getAllDisputes(): Flow<List<DisputeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDispute(dispute: DisputeEntity)

    @Update
    suspend fun updateDispute(dispute: DisputeEntity)
}

@Dao
interface InstitutionalDao {
    @Query("SELECT * FROM institutional_bookings ORDER BY createdAt DESC")
    fun getAllInstitutionalBookings(): Flow<List<InstitutionalBookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstitutionalBooking(booking: InstitutionalBookingEntity): Long

    @Update
    suspend fun updateInstitutionalBooking(booking: InstitutionalBookingEntity)
}

@Dao
interface DemandForecastDao {
    @Query("SELECT * FROM demand_forecasts ORDER BY shortage DESC")
    fun getAllForecasts(): Flow<List<DemandForecastEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecasts(forecasts: List<DemandForecastEntity>)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE recipientRole = :role OR recipientRole = 'ALL' ORDER BY timestamp DESC")
    fun getNotificationsForRole(role: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)
}
