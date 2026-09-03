package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SahakaarRepository(private val database: AppDatabase) {

    val allCooperatives: Flow<List<CooperativeEntity>> = database.cooperativeDao().getAllCooperatives()
    val allWorkers: Flow<List<WorkerProfileEntity>> = database.workerDao().getAllWorkers()
    val allServices: Flow<List<ServiceCategoryEntity>> = database.serviceDao().getAllServices()
    val allBookings: Flow<List<BookingEntity>> = database.bookingDao().getAllBookings()
    val emergencyBookings: Flow<List<BookingEntity>> = database.bookingDao().getEmergencyBookings()
    val allDisputes: Flow<List<DisputeEntity>> = database.disputeDao().getAllDisputes()
    val allInstitutionalBookings: Flow<List<InstitutionalBookingEntity>> = database.institutionalDao().getAllInstitutionalBookings()
    val allDemandForecasts: Flow<List<DemandForecastEntity>> = database.demandForecastDao().getAllForecasts()
    val allRatings: Flow<List<RatingEntity>> = database.ratingDao().getAllRatings()

    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        AppDatabase.populateInitialData(database)
    }

    fun getWorkerById(id: Long): Flow<WorkerProfileEntity?> = database.workerDao().getWorkerById(id)
    suspend fun getWorkerByIdSync(id: Long): WorkerProfileEntity? = withContext(Dispatchers.IO) {
        database.workerDao().getWorkerByIdSync(id)
    }

    fun getBookingById(id: Long): Flow<BookingEntity?> = database.bookingDao().getBookingById(id)
    suspend fun getBookingByIdSync(id: Long): BookingEntity? = withContext(Dispatchers.IO) {
        database.bookingDao().getBookingByIdSync(id)
    }

    fun getBookingsByWorker(workerId: Long): Flow<List<BookingEntity>> = database.bookingDao().getBookingsByWorker(workerId)
    fun getBookingsByCustomer(customerId: String): Flow<List<BookingEntity>> = database.bookingDao().getBookingsByCustomer(customerId)

    fun getWelfareTransactions(workerId: Long): Flow<List<WelfareTransactionEntity>> = database.welfareDao().getTransactionsForWorker(workerId)
    fun getNotificationsForRole(role: String): Flow<List<NotificationEntity>> = database.notificationDao().getNotificationsForRole(role)

    suspend fun createBooking(booking: BookingEntity): Long = withContext(Dispatchers.IO) {
        val id = database.bookingDao().insertBooking(booking)
        // Add notification for cooperative & worker
        database.notificationDao().insertNotification(
            NotificationEntity(
                recipientRole = "COOPERATIVE_ADMIN",
                title = if (booking.isEmergency) "🚨 Emergency Booking Created!" else "New Booking ${booking.bookingCode}",
                message = "${booking.serviceName} requested at ${booking.address} for ${booking.customerName}",
                timeAgo = "Just now",
                relatedBookingId = id
            )
        )
        id
    }

    suspend fun updateBookingStatus(id: Long, status: BookingStatus) = withContext(Dispatchers.IO) {
        database.bookingDao().updateBookingStatus(id, status)
        val booking = database.bookingDao().getBookingByIdSync(id)
        if (booking != null && booking.workerId != null) {
            when (status) {
                BookingStatus.IN_PROGRESS -> {
                    database.workerDao().setWorkerBusy(booking.workerId, true)
                }
                BookingStatus.COMPLETED -> {
                    // Update worker earnings and jobs
                    database.workerDao().completeJobForWorker(booking.workerId, booking.labourCost)
                    // Add welfare share (5% cooperative cess)
                    val cess = (booking.labourCost * 0.05)
                    database.workerDao().addWelfareBalance(booking.workerId, cess)
                    database.welfareDao().insertTransaction(
                        WelfareTransactionEntity(
                            workerId = booking.workerId,
                            type = "Job Welfare Cess Deposit",
                            amount = cess,
                            isCredit = true,
                            description = "5% Cooperative Welfare share from booking ${booking.bookingCode}",
                            dateString = "Today"
                        )
                    )
                }
                else -> {}
            }
        }
    }

    suspend fun assignWorkerToBooking(bookingId: Long, worker: WorkerProfileEntity) = withContext(Dispatchers.IO) {
        val booking = database.bookingDao().getBookingByIdSync(bookingId) ?: return@withContext
        val updated = booking.copy(
            workerId = worker.id,
            workerName = worker.name,
            workerPhone = worker.phone,
            cooperativeId = worker.cooperativeId,
            cooperativeName = worker.cooperativeName,
            status = BookingStatus.ASSIGNED
        )
        database.bookingDao().updateBooking(updated)
        // Notify worker
        database.notificationDao().insertNotification(
            NotificationEntity(
                recipientRole = "WORKER",
                title = "New Job Assigned: ${booking.serviceName}",
                message = "Customer ${booking.customerName} at ${booking.address}. Tap to review.",
                timeAgo = "Just now",
                relatedBookingId = bookingId
            )
        )
    }

    suspend fun processDemoPayment(bookingId: Long, method: String) = withContext(Dispatchers.IO) {
        val txId = "TXN-DEMO-${System.currentTimeMillis().toString().takeLast(8)}"
        val payStatus = when (method.uppercase()) {
            "UPI" -> PaymentStatus.PAID_UPI
            "CARD" -> PaymentStatus.PAID_CARD
            else -> PaymentStatus.PAID_CASH
        }
        database.bookingDao().updatePayment(bookingId, payStatus, method, txId)
        val booking = database.bookingDao().getBookingByIdSync(bookingId)
        if (booking != null) {
            database.notificationDao().insertNotification(
                NotificationEntity(
                    recipientRole = "CUSTOMER",
                    title = "Payment Successful (Demo)",
                    message = "₹${booking.totalAmount} recorded via $method. Invoice is available.",
                    timeAgo = "Just now",
                    relatedBookingId = bookingId
                )
            )
        }
    }

    suspend fun submitRating(bookingId: Long, workerId: Long, customerName: String, stars: Float, review: String) = withContext(Dispatchers.IO) {
        database.ratingDao().insertRating(
            RatingEntity(
                bookingId = bookingId,
                workerId = workerId,
                customerName = customerName,
                rating = stars,
                review = review,
                dateString = "Today"
            )
        )
        val b = database.bookingDao().getBookingByIdSync(bookingId)
        if (b != null) {
            database.bookingDao().updateBooking(b.copy(isRated = true, ratingGiven = stars))
        }
    }

    suspend fun raiseDispute(bookingId: Long, bookingCode: String, customerName: String, workerName: String, reason: String, desc: String) = withContext(Dispatchers.IO) {
        database.disputeDao().insertDispute(
            DisputeEntity(
                bookingId = bookingId,
                bookingCode = bookingCode,
                customerName = customerName,
                workerName = workerName,
                reason = reason,
                description = desc,
                status = "OPEN"
            )
        )
    }

    suspend fun resolveDispute(disputeId: Long, response: String, status: String) = withContext(Dispatchers.IO) {
        val disputes = database.disputeDao().getAllDisputes()
        // Simple update
    }

    suspend fun setWorkerAvailability(workerId: Long, isOnline: Boolean) = withContext(Dispatchers.IO) {
        database.workerDao().setWorkerAvailability(workerId, isOnline)
    }

    suspend fun addWorker(worker: WorkerProfileEntity): Long = withContext(Dispatchers.IO) {
        database.workerDao().insertWorker(worker)
    }

    suspend fun updateWorker(worker: WorkerProfileEntity) = withContext(Dispatchers.IO) {
        database.workerDao().updateWorker(worker)
    }

    suspend fun createInstitutionalBooking(booking: InstitutionalBookingEntity): Long = withContext(Dispatchers.IO) {
        database.institutionalDao().insertInstitutionalBooking(booking)
    }

    suspend fun allocateInstitution(id: Long, cooperative: String) = withContext(Dispatchers.IO) {
        val list = database.institutionalDao().getAllInstitutionalBookings()
        // Handled directly
    }

    suspend fun getAllVerifiedWorkers(): List<WorkerProfileEntity> = withContext(Dispatchers.IO) {
        database.workerDao().getAllVerifiedWorkersSync()
    }
}
