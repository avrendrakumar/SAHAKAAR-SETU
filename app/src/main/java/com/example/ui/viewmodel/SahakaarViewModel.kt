package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.SahakaarRepository
import com.example.engine.AiClassificationResult
import com.example.engine.AiServiceClassifier
import com.example.engine.WorkerMatchResult
import com.example.engine.WorkerMatchingEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SahakaarViewModel(private val repository: SahakaarRepository) : ViewModel() {

    // Current Role
    private val _currentRole = MutableStateFlow(UserRole.CUSTOMER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    // Active Worker ID (for worker mode)
    private val _activeWorkerId = MutableStateFlow(1L)
    val activeWorkerId: StateFlow<Long> = _activeWorkerId.asStateFlow()

    // Selected Worker for Detail / Skill Passport modal
    private val _selectedWorker = MutableStateFlow<WorkerProfileEntity?>(null)
    val selectedWorker: StateFlow<WorkerProfileEntity?> = _selectedWorker.asStateFlow()

    // Selected Booking for Tracker / Invoice / Rating
    private val _selectedBooking = MutableStateFlow<BookingEntity?>(null)
    val selectedBooking: StateFlow<BookingEntity?> = _selectedBooking.asStateFlow()

    // AI Classification result
    private val _aiClassification = MutableStateFlow<AiClassificationResult?>(null)
    val aiClassification: StateFlow<AiClassificationResult?> = _aiClassification.asStateFlow()

    // Toast / Banner message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Flows from repository
    val cooperatives = repository.allCooperatives.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val workers = repository.allWorkers.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val services = repository.allServices.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val bookings = repository.allBookings.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val emergencyBookings = repository.emergencyBookings.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val disputes = repository.allDisputes.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val institutionalBookings = repository.allInstitutionalBookings.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val demandForecasts = repository.allDemandForecasts.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val activeWorker: StateFlow<WorkerProfileEntity?> = combine(workers, activeWorkerId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val workerBookings: StateFlow<List<BookingEntity>> = combine(bookings, activeWorkerId) { list, id ->
        list.filter { it.workerId == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customerBookings: StateFlow<List<BookingEntity>> = bookings.map { list ->
        list.filter { it.customerId.startsWith("cust_1") || it.customerName.contains("Rajesh", ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workerWelfareTransactions: StateFlow<List<WelfareTransactionEntity>> = activeWorkerId.flatMapLatest { id ->
        repository.getWelfareTransactions(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentRoleNotifications: StateFlow<List<NotificationEntity>> = currentRole.flatMapLatest { role ->
        repository.getNotificationsForRole(role.name)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    fun setRole(role: UserRole) {
        _currentRole.value = role
        _userMessage.value = "Switched to ${role.displayName} view"
    }

    fun selectWorker(worker: WorkerProfileEntity?) {
        _selectedWorker.value = worker
    }

    fun selectBooking(booking: BookingEntity?) {
        _selectedBooking.value = booking
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    // AI Classification
    fun classifyCustomerProblem(text: String) {
        if (text.isBlank()) {
            _aiClassification.value = null
            return
        }
        val res = AiServiceClassifier.classifyService(text)
        _aiClassification.value = res
    }

    fun clearAiClassification() {
        _aiClassification.value = null
    }

    // Intelligent Matching
    fun getRankedWorkersForService(serviceName: String, isEmergency: Boolean = false): List<WorkerMatchResult> {
        val allW = workers.value
        return WorkerMatchingEngine.matchWorkers(
            workers = allW,
            requiredSkill = serviceName,
            isEmergency = isEmergency
        )
    }

    // Customer Actions
    fun createServiceBooking(
        serviceName: String,
        problem: String,
        address: String,
        isEmergency: Boolean,
        selectedWorker: WorkerProfileEntity?,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val code = "BK-2026-${(1000..9999).random()}"
            val baseLabour = if (isEmergency) 500.0 else 350.0
            val fee = baseLabour * 0.08
            val total = baseLabour + fee

            val newBooking = BookingEntity(
                bookingCode = code,
                customerId = "cust_1",
                customerName = "Rajesh Sharma",
                customerPhone = "+91 94310 88219",
                serviceName = serviceName,
                problemDescription = problem.ifBlank { "Service requested via Sahakaar Setu" },
                address = address.ifBlank { "Boring Road, Patna" },
                district = "Patna",
                isEmergency = isEmergency,
                status = if (selectedWorker != null) BookingStatus.ASSIGNED else BookingStatus.PENDING,
                workerId = selectedWorker?.id,
                workerName = selectedWorker?.name,
                workerPhone = selectedWorker?.phone,
                cooperativeId = selectedWorker?.cooperativeId ?: 1L,
                cooperativeName = selectedWorker?.cooperativeName ?: "Patna Shramik Sahakari Samiti",
                scheduledDate = if (isEmergency) "Immediate Dispatch" else "Today, Scheduled",
                scheduledTime = if (isEmergency) "Within 20 mins" else "11:00 AM",
                labourCost = baseLabour,
                materialCost = 0.0,
                platformFee = (fee * 10).toInt() / 10.0,
                totalAmount = (total * 10).toInt() / 10.0,
                paymentStatus = PaymentStatus.PENDING
            )

            val id = repository.createBooking(newBooking)
            _selectedBooking.value = newBooking.copy(id = id)
            _userMessage.value = if (isEmergency) "🚨 Emergency dispatch initiated!" else "Booking $code confirmed!"
            onSuccess(id)
        }
    }

    // Worker Actions
    fun toggleWorkerAvailability(workerId: Long, currentOnline: Boolean) {
        viewModelScope.launch {
            repository.setWorkerAvailability(workerId, !currentOnline)
            _userMessage.value = if (!currentOnline) "Status: ONLINE & Ready for Jobs" else "Status: OFFLINE"
        }
    }

    fun updateBookingStatus(bookingId: Long, newStatus: BookingStatus) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, newStatus)
            // Refresh selected booking if matching
            val updated = repository.getBookingByIdSync(bookingId)
            _selectedBooking.value = updated
            _userMessage.value = "Status updated to: ${newStatus.displayName}"
        }
    }

    fun assignWorker(bookingId: Long, worker: WorkerProfileEntity) {
        viewModelScope.launch {
            repository.assignWorkerToBooking(bookingId, worker)
            val updated = repository.getBookingByIdSync(bookingId)
            _selectedBooking.value = updated
            _userMessage.value = "${worker.name} assigned to booking"
        }
    }

    // Demo Payment
    fun processDemoPayment(bookingId: Long, method: String) {
        viewModelScope.launch {
            repository.processDemoPayment(bookingId, method)
            val updated = repository.getBookingByIdSync(bookingId)
            _selectedBooking.value = updated
            _userMessage.value = "Demo Payment Successful via $method"
        }
    }

    // Rating
    fun submitRating(bookingId: Long, workerId: Long, rating: Float, review: String) {
        viewModelScope.launch {
            repository.submitRating(bookingId, workerId, "Rajesh Sharma", rating, review)
            val updated = repository.getBookingByIdSync(bookingId)
            _selectedBooking.value = updated
            _userMessage.value = "Rating of $rating ⭐ submitted! Thank you."
        }
    }

    // Dispute
    fun raiseDispute(bookingId: Long, code: String, workerName: String, reason: String, desc: String) {
        viewModelScope.launch {
            repository.raiseDispute(bookingId, code, "Rajesh Sharma", workerName, reason, desc)
            _userMessage.value = "Dispute raised. Cooperative Admin will review shortly."
        }
    }

    // Worker management
    fun addNewWorker(
        name: String,
        phone: String,
        skill: String,
        coopId: Long,
        coopName: String,
        expYears: Int,
        hourlyRate: Double
    ) {
        viewModelScope.launch {
            val code = "SS-${coopName.take(3).uppercase()}-2026-${(100..999).random()}"
            val newW = WorkerProfileEntity(
                workerCode = code,
                name = name,
                phone = phone,
                email = "${name.lowercase().replace(" ", ".")}@sahakaar.worker.in",
                cooperativeId = coopId,
                cooperativeName = coopName,
                primarySkill = skill,
                secondarySkills = "Safety Protocol, General Repairs",
                experienceYears = expYears,
                rating = 5.0f,
                reviewCount = 0,
                reliabilityScore = 95,
                isVerified = true,
                verificationDate = "Today",
                certList = "Cooperative Verified Skill Certificate, Safety Induction Pass",
                isOnline = true,
                isBusy = false,
                isEmergencyReady = true,
                completedJobs = 0,
                welfareBalance = 1000.0,
                dailyEarnings = 0.0,
                hourlyRate = hourlyRate,
                district = coopName.split(" ").first()
            )
            repository.addWorker(newW)
            _userMessage.value = "Worker $name enrolled with ID $code"
        }
    }

    fun verifyWorker(worker: WorkerProfileEntity) {
        viewModelScope.launch {
            val updated = worker.copy(isVerified = true, verificationDate = "Today")
            repository.updateWorker(updated)
            _selectedWorker.value = updated
            _userMessage.value = "${worker.name} verified by Cooperative Authority"
        }
    }

    fun toggleWorkerSuspension(worker: WorkerProfileEntity) {
        viewModelScope.launch {
            val updated = worker.copy(isOnline = !worker.isOnline)
            repository.updateWorker(updated)
            _selectedWorker.value = updated
            _userMessage.value = if (!updated.isOnline) "Worker suspended from job dispatch" else "Worker activated"
        }
    }

    // Institutional Booking
    fun createInstitutionalBooking(
        name: String,
        type: String,
        person: String,
        phone: String,
        trade: String,
        count: Int,
        days: Int,
        location: String
    ) {
        viewModelScope.launch {
            val cost = (count * days * 1200.0)
            val req = InstitutionalBookingEntity(
                institutionName = name,
                institutionType = type,
                contactPerson = person,
                phone = phone,
                tradeRequired = trade,
                workerCount = count,
                durationDays = days,
                location = location,
                status = "PENDING",
                estimatedBudget = cost,
                cooperativeAllocated = "Patna Shramik Sahakari Samiti"
            )
            repository.createInstitutionalBooking(req)
            _userMessage.value = "Institutional request for $count $trade workers submitted!"
        }
    }
}
