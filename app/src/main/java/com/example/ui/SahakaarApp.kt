package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.AppDatabase
import com.example.data.model.UserRole
import com.example.data.repository.SahakaarRepository
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.SahakaarNavy
import com.example.ui.viewmodel.SahakaarViewModel
import com.example.ui.viewmodel.SahakaarViewModelFactory
import kotlinx.coroutines.launch

enum class ScreenState {
    DASHBOARD,
    SERVICE_BOOKING,
    BOOKING_TRACKER,
    LIVE_MAP,
    WELFARE_WALLET
}

@Composable
fun SahakaarApp() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getInstance(context) }
    val repository = remember { SahakaarRepository(database) }
    val viewModel: SahakaarViewModel = viewModel(factory = SahakaarViewModelFactory(repository))

    val currentRole by viewModel.currentRole.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val selectedWorker by viewModel.selectedWorker.collectAsState()
    val selectedBooking by viewModel.selectedBooking.collectAsState()
    val aiClassification by viewModel.aiClassification.collectAsState()

    val cooperatives by viewModel.cooperatives.collectAsState()
    val workers by viewModel.workers.collectAsState()
    val services by viewModel.services.collectAsState()
    val bookings by viewModel.bookings.collectAsState()
    val disputes by viewModel.disputes.collectAsState()
    val institutionalBookings by viewModel.institutionalBookings.collectAsState()
    val demandForecasts by viewModel.demandForecasts.collectAsState()
    val notifications by viewModel.currentRoleNotifications.collectAsState()

    val activeWorker by viewModel.activeWorker.collectAsState()
    val workerBookings by viewModel.workerBookings.collectAsState()
    val customerBookings by viewModel.customerBookings.collectAsState()
    val workerWelfareTransactions by viewModel.workerWelfareTransactions.collectAsState()

    var currentScreen by remember { mutableStateOf(ScreenState.DASHBOARD) }
    var bookingServiceToOrder by remember { mutableStateOf("Plumbing") }
    var bookingIsEmergency by remember { mutableStateOf(false) }

    // Modals
    var showPaymentSheet by remember { mutableStateOf(false) }
    var showInvoiceDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var showDisputeDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Observe user message
    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            RoleSwitchHeader(
                currentRole = currentRole,
                onRoleSelected = { role ->
                    viewModel.setRole(role)
                    currentScreen = ScreenState.DASHBOARD
                },
                notifications = notifications,
                onNotificationClick = { notif ->
                    notif.relatedBookingId?.let { bId ->
                        val b = bookings.find { it.id == bId }
                        if (b != null) {
                            viewModel.selectBooking(b)
                            currentScreen = ScreenState.BOOKING_TRACKER
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentRole) {
                UserRole.CUSTOMER -> {
                    when (currentScreen) {
                        ScreenState.DASHBOARD -> {
                            CustomerDashboardScreen(
                                services = services,
                                activeBookings = customerBookings,
                                aiResult = aiClassification,
                                onClassifyProblem = { viewModel.classifyCustomerProblem(it) },
                                onClearAiClassification = { viewModel.clearAiClassification() },
                                onSelectServiceToBook = { service, isEmergency ->
                                    bookingServiceToOrder = service
                                    bookingIsEmergency = isEmergency
                                    currentScreen = ScreenState.SERVICE_BOOKING
                                },
                                onTrackBooking = { booking ->
                                    viewModel.selectBooking(booking)
                                    currentScreen = ScreenState.BOOKING_TRACKER
                                },
                                onViewAllBookings = {
                                    val top = customerBookings.firstOrNull() ?: bookings.firstOrNull()
                                    if (top != null) {
                                        viewModel.selectBooking(top)
                                        currentScreen = ScreenState.BOOKING_TRACKER
                                    }
                                },
                                onOpenLiveMap = {
                                    currentScreen = ScreenState.LIVE_MAP
                                }
                            )
                        }
                        ScreenState.SERVICE_BOOKING -> {
                            val ranked = remember(bookingServiceToOrder, bookingIsEmergency, workers) {
                                viewModel.getRankedWorkersForService(bookingServiceToOrder, bookingIsEmergency)
                            }
                            ServiceBookingScreen(
                                initialService = bookingServiceToOrder,
                                initialEmergency = bookingIsEmergency,
                                services = services,
                                rankedWorkers = ranked,
                                onWorkerPassportClick = { w -> viewModel.selectWorker(w) },
                                onConfirmBooking = { sName, prob, addr, emerg, w ->
                                    viewModel.createServiceBooking(
                                        serviceName = sName,
                                        problem = prob,
                                        address = addr,
                                        isEmergency = emerg,
                                        selectedWorker = w,
                                        onSuccess = {
                                            currentScreen = ScreenState.BOOKING_TRACKER
                                        }
                                    )
                                },
                                onBack = { currentScreen = ScreenState.DASHBOARD }
                            )
                        }
                        ScreenState.BOOKING_TRACKER -> {
                            val activeB = selectedBooking ?: customerBookings.firstOrNull() ?: bookings.firstOrNull()
                            if (activeB != null) {
                                val assignedWorker = workers.find { it.id == activeB.workerId }
                                BookingTrackerScreen(
                                    booking = activeB,
                                    worker = assignedWorker,
                                    onAdvanceStatus = { nextStatus ->
                                        viewModel.updateBookingStatus(activeB.id, nextStatus)
                                    },
                                    onOpenPayment = { showPaymentSheet = true },
                                    onOpenInvoice = { showInvoiceDialog = true },
                                    onOpenRating = { showRatingDialog = true },
                                    onOpenDispute = { showDisputeDialog = true },
                                    onOpenPassport = { w -> viewModel.selectWorker(w) },
                                    onBack = { currentScreen = ScreenState.DASHBOARD }
                                )
                            }
                        }
                        ScreenState.LIVE_MAP -> {
                            LiveWorkerMapScreen(
                                workers = workers,
                                cooperatives = cooperatives,
                                onWorkerSelected = { w -> viewModel.selectWorker(w) }
                            )
                        }
                        else -> {}
                    }
                }

                UserRole.WORKER -> {
                    when (currentScreen) {
                        ScreenState.WELFARE_WALLET -> {
                            WelfareWalletScreen(
                                worker = activeWorker,
                                transactions = workerWelfareTransactions,
                                onBack = { currentScreen = ScreenState.DASHBOARD }
                            )
                        }
                        else -> {
                            WorkerDashboardScreen(
                                worker = activeWorker,
                                bookings = workerBookings.ifEmpty { bookings.take(3) },
                                welfareTransactions = workerWelfareTransactions,
                                onToggleAvailability = { wId, curr ->
                                    viewModel.toggleWorkerAvailability(wId, curr)
                                },
                                onUpdateBookingStatus = { bId, st ->
                                    viewModel.updateBookingStatus(bId, st)
                                },
                                onOpenPassport = { w -> viewModel.selectWorker(w) },
                                onOpenWelfareWallet = { currentScreen = ScreenState.WELFARE_WALLET }
                            )
                        }
                    }
                }

                UserRole.COOPERATIVE_ADMIN -> {
                    when (currentScreen) {
                        ScreenState.LIVE_MAP -> {
                            LiveWorkerMapScreen(
                                workers = workers,
                                cooperatives = cooperatives,
                                onWorkerSelected = { w -> viewModel.selectWorker(w) }
                            )
                        }
                        else -> {
                            CooperativeDashboardScreen(
                                cooperatives = cooperatives,
                                workers = workers,
                                bookings = bookings,
                                disputes = disputes,
                                demandForecasts = demandForecasts,
                                institutionalBookings = institutionalBookings,
                                onOpenPassport = { w -> viewModel.selectWorker(w) },
                                onVerifyWorker = { w -> viewModel.verifyWorker(w) },
                                onToggleSuspend = { w -> viewModel.toggleWorkerSuspension(w) },
                                onAssignWorkerToBooking = { bId, w -> viewModel.assignWorker(bId, w) },
                                onAddNewWorker = { name, phone, skill, cId, cName, exp, rate ->
                                    viewModel.addNewWorker(name, phone, skill, cId, cName, exp, rate)
                                },
                                onOpenLiveMap = { currentScreen = ScreenState.LIVE_MAP }
                            )
                        }
                    }
                }

                UserRole.FEDERATION_ADMIN, UserRole.SUPER_ADMIN -> {
                    FederationDashboardScreen(
                        cooperatives = cooperatives,
                        workers = workers,
                        bookings = bookings,
                        disputes = disputes,
                        onShowMessage = { msg -> viewModel.showMessage(msg) }
                    )
                }
            }

            // Global Dialogs & Modals
            // 1. Digital Skill Passport Modal
            selectedWorker?.let { w ->
                SkillPassportDialog(
                    worker = w,
                    onDismiss = { viewModel.selectWorker(null) }
                )
            }

            // 2. Demo Payment Sheet
            if (showPaymentSheet && selectedBooking != null) {
                DemoPaymentSheet(
                    booking = selectedBooking!!,
                    onPaymentSuccess = { method ->
                        viewModel.processDemoPayment(selectedBooking!!.id, method)
                        showPaymentSheet = false
                    },
                    onDismiss = { showPaymentSheet = false }
                )
            }

            // 3. Digital Invoice Modal
            if (showInvoiceDialog && selectedBooking != null) {
                DigitalInvoiceDialog(
                    booking = selectedBooking!!,
                    onDismiss = { showInvoiceDialog = false },
                    onShowMessage = { msg -> viewModel.showMessage(msg) }
                )
            }

            // 4. Rating Review Modal
            if (showRatingDialog && selectedBooking != null) {
                val b = selectedBooking!!
                RatingReviewDialog(
                    booking = b,
                    onSubmit = { stars, rev ->
                        viewModel.submitRating(b.id, b.workerId ?: 1L, stars, rev)
                        showRatingDialog = false
                    },
                    onDismiss = { showRatingDialog = false }
                )
            }

            // 5. Dispute Modal
            if (showDisputeDialog && selectedBooking != null) {
                val b = selectedBooking!!
                DisputeDialog(
                    booking = b,
                    onSubmit = { reason, desc ->
                        viewModel.raiseDispute(b.id, b.bookingCode, b.workerName ?: "Worker", reason, desc)
                        showDisputeDialog = false
                    },
                    onDismiss = { showDisputeDialog = false }
                )
            }
        }
    }
}
