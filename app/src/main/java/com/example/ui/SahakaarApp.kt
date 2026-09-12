package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.DatabaseSeeder
import com.example.data.model.*
import com.example.data.repository.SahakaarRepository
import com.example.ui.components.LiveWorkerMap
import com.example.ui.components.NotificationCenterDialog
import com.example.ui.components.SahakaarTopAppBar
import com.example.ui.components.UserProfileDialog
import com.example.ui.screens.LandingScreen
import com.example.ui.screens.ai.GeminiChatScreen
import com.example.ui.screens.auth.LoginPortalScreen
import com.example.ui.screens.cooperative.CooperativeControlCenterScreen
import com.example.ui.screens.customer.CustomerHomeScreen
import com.example.ui.screens.federation.FederationDashboardScreen
import com.example.ui.screens.institution.InstitutionalMarketplaceScreen
import com.example.ui.screens.worker.WorkerDashboardScreen
import com.example.ui.theme.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun SahakaarApp(
    repository: SahakaarRepository,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Seed database on startup if needed
    LaunchedEffect(Unit) {
        try {
            repository.ensureDataSeeded()
        } catch (_: CancellationException) {
            // Safe cancellation when leaving composition
        }
    }

    // Language State
    var currentLanguage by remember { mutableStateOf(AppLanguage.ENGLISH) }

    // Authentication & Role State - Defaults to NOT logged in as requested
    var isLoggedIn by remember { mutableStateOf(false) }
    var showLoginPortal by remember { mutableStateOf(true) }
    var authenticatedUser by remember { mutableStateOf<UserEntity?>(null) }
    var currentRole by remember { mutableStateOf("CUSTOMER") }
    var activeNavDestination by remember { mutableStateOf("HOME") } // "HOME", "DASHBOARD", "INSTITUTION", "MAP"

    // Collect Reactive State Flows
    val cooperatives by repository.allCooperatives.collectAsState(initial = emptyList())
    val categories by repository.allCategories.collectAsState(initial = emptyList())
    val allWorkers by repository.allWorkers.collectAsState(initial = emptyList())
    val allBookings by repository.allBookings.collectAsState(initial = emptyList())
    val demandForecasts by repository.allForecasts.collectAsState(initial = emptyList())
    val institutionalBookings by repository.allInstitutionalBookings.collectAsState(initial = emptyList())
    val disputes by repository.allDisputes.collectAsState(initial = emptyList())
    val professions by repository.allProfessions.collectAsState(initial = emptyList())

    // Notification & Profile dialog states
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

    // Active User based on authenticated user or role fallback
    val currentUser = remember(authenticatedUser, currentRole) {
        authenticatedUser ?: when (currentRole) {
            "WORKER" -> DatabaseSeeder.getInitialUsers().find { it.role == "WORKER" }!!
            "COOPERATIVE_ADMIN" -> DatabaseSeeder.getInitialUsers().find { it.role == "COOPERATIVE_ADMIN" }!!
            "FEDERATION_ADMIN" -> DatabaseSeeder.getInitialUsers().find { it.role == "FEDERATION_ADMIN" }!!
            "SUPER_ADMIN" -> DatabaseSeeder.getInitialUsers().find { it.role == "SUPER_ADMIN" }!!
            "INSTITUTION" -> UserEntity("u_inst_live", "b2b@patna.demo", "Patna Central Model School", "INSTITUTION", "+91 94312 88990", "Patna")
            else -> DatabaseSeeder.getInitialUsers().find { it.role == "CUSTOMER" }!!
        }
    }

    val userNotifications by repository.getNotificationsForUser(currentUser.id, currentUser.role)
        .collectAsState(initial = emptyList())
    val unreadNotificationCount = remember(userNotifications) {
        userNotifications.count { !it.isRead }
    }

    // Role-specific data
    val customerBookings = remember(allBookings, currentUser) {
        if (currentRole == "CUSTOMER") {
            allBookings.filter { it.customerId == currentUser.id }
        } else {
            allBookings
        }
    }

    val currentWorkerProfile = remember(allWorkers, currentUser) {
        allWorkers.find { it.userId == currentUser.id }
            ?: allWorkers.find { it.phone == currentUser.phone }
            ?: allWorkers.firstOrNull()
    }

    val workerBookings = remember(allBookings, currentWorkerProfile) {
        val wId = currentWorkerProfile?.id ?: "w_raj"
        allBookings.filter { it.workerId == wId }
    }

    val availableWork = remember(allBookings, currentWorkerProfile) {
        val wId = currentWorkerProfile?.id ?: "w_raj"
        allBookings.filter { b ->
            (b.status in listOf("PENDING", "REQUESTED") || b.workerId.isBlank()) && b.workerId != wId
        }
    }

    val welfareWallet by if (currentWorkerProfile != null) {
        repository.getWelfareWallet(currentWorkerProfile.id).collectAsState(initial = null)
    } else {
        remember { mutableStateOf(null) }
    }

    val welfareTransactions by if (currentWorkerProfile != null) {
        repository.getWelfareTransactions(currentWorkerProfile.id).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    var textScaleFactor by remember { mutableFloatStateOf(1.0f) }
    val onToggleTextSize: () -> Unit = {
        textScaleFactor = when (textScaleFactor) {
            1.0f -> 1.15f
            1.15f -> 1.30f
            else -> 1.0f
        }
        val pct = (textScaleFactor * 100).toInt()
        coroutineScope.launch {
            snackbarHostState.showSnackbar("App text size set to $pct%")
        }
    }

    val currentDensity = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(
            density = currentDensity.density,
            fontScale = currentDensity.fontScale * textScaleFactor
        )
    ) {
    // Display dedicated Login & Customer/User Portal if user is not logged in or requested login portal
    if (showLoginPortal && !isLoggedIn) {
        LoginPortalScreen(
            language = currentLanguage,
            cooperatives = cooperatives,
            professions = professions,
            onLanguageToggle = {
                currentLanguage = if (currentLanguage == AppLanguage.ENGLISH) AppLanguage.HINDI else AppLanguage.ENGLISH
            },
            onToggleTextSize = onToggleTextSize,
            onLoginSuccess = { user, role ->
                authenticatedUser = user
                currentRole = role
                isLoggedIn = true
                showLoginPortal = false
                if (role == "WORKER") {
                    activeNavDestination = "WORKER_JOBS"
                } else if (role == "INSTITUTION") {
                    activeNavDestination = "INSTITUTION"
                } else if (role in listOf("COOPERATIVE_ADMIN", "FEDERATION_ADMIN", "SUPER_ADMIN")) {
                    activeNavDestination = "DASHBOARD"
                } else {
                    activeNavDestination = "CUSTOMER_WORKERS"
                }
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Welcome, ${user.name}! Signed in successfully.")
                }
            },
            onPerformLogin = { identifier, password ->
                repository.loginWithSupabase(identifier, password)
            },
            onRegisterCustomer = { name, phone, email, pass, state, dist, vill ->
                coroutineScope.launch {
                    try {
                        val newUser = repository.registerCustomer(name, phone, email, pass, state, dist, vill)
                        authenticatedUser = newUser
                        currentRole = "CUSTOMER"
                        isLoggedIn = true
                        showLoginPortal = false
                        activeNavDestination = "CUSTOMER_WORKERS"
                        snackbarHostState.showSnackbar("Welcome ${newUser.name}! ID: ${newUser.customIdTag}")
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Registration failed: ${e.localizedMessage}")
                    }
                }
            },
            onRegisterWorker = { name, phone, email, pass, state, dist, vill, prof, exp, rate, coopId ->
                coroutineScope.launch {
                    try {
                        val (newUser, profile) = repository.registerWorkerFull(
                            name, phone, email, pass, state, dist, vill, prof, listOf(prof), exp, rate, coopId
                        )
                        authenticatedUser = newUser
                        currentRole = "WORKER"
                        isLoggedIn = true
                        showLoginPortal = false
                        activeNavDestination = "WORKER_JOBS"
                        snackbarHostState.showSnackbar("Welcome ${newUser.name}! Artisan ID: ${profile.workerIdTag}")
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Worker registration failed: ${e.localizedMessage}")
                    }
                }
            },
            onContinueAsGuest = {
                isLoggedIn = false
                showLoginPortal = false
                currentRole = "CUSTOMER"
                activeNavDestination = "CUSTOMER_WORKERS"
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Browsing as Guest. Tap 'Log In' anytime to access customer services.")
                }
            }
        )
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = ElegantDarkBg,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                SahakaarTopAppBar(
                    currentRole = currentRole,
                    currentLanguage = currentLanguage,
                    userName = if (isLoggedIn) currentUser.name else null,
                    isLoggedIn = isLoggedIn,
                    unreadNotificationCount = unreadNotificationCount,
                    onLanguageToggle = {
                        currentLanguage = if (currentLanguage == AppLanguage.ENGLISH) AppLanguage.HINDI else AppLanguage.ENGLISH
                    },
                    onRoleChange = { newRole ->
                        currentRole = newRole
                        isLoggedIn = true
                        authenticatedUser = when (newRole) {
                            "WORKER" -> DatabaseSeeder.getInitialUsers().find { it.role == "WORKER" }
                            "COOPERATIVE_ADMIN" -> DatabaseSeeder.getInitialUsers().find { it.role == "COOPERATIVE_ADMIN" }
                            "FEDERATION_ADMIN" -> DatabaseSeeder.getInitialUsers().find { it.role == "FEDERATION_ADMIN" }
                            "SUPER_ADMIN" -> DatabaseSeeder.getInitialUsers().find { it.role == "SUPER_ADMIN" }
                            "INSTITUTION" -> UserEntity("u_inst_demo", "school@patna.demo", "Patna Central Model School", "INSTITUTION", "+91 94312 88990", "Patna")
                            else -> DatabaseSeeder.getInitialUsers().find { it.role == "CUSTOMER" }
                        }
                        if (newRole == "INSTITUTION") {
                            activeNavDestination = "INSTITUTION"
                        } else if (newRole != "CUSTOMER") {
                            activeNavDestination = "DASHBOARD"
                        }
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Switched profile to $newRole")
                        }
                    },
                    onOpenNotifications = {
                        showNotificationDialog = true
                    },
                    onOpenProfile = {
                        showProfileDialog = true
                    },
                    onOpenAiAssistant = {
                        activeNavDestination = "AI_ASSISTANT"
                    },
                    onLogout = {
                        isLoggedIn = false
                        authenticatedUser = null
                        showLoginPortal = true
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Logged out successfully.")
                        }
                    },
                    onLoginClick = {
                        showLoginPortal = true
                    },
                    onToggleTextSize = onToggleTextSize
                )
            },
        bottomBar = {
            NavigationBar(
                containerColor = ElegantDarkSurface,
                contentColor = ElegantTextPrimary,
                tonalElevation = 0.dp,
                modifier = Modifier.drawBehind {
                    drawLine(
                        color = ElegantDarkBorder,
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            ) {
                if (currentRole == "WORKER") {
                    NavigationBarItem(
                        selected = activeNavDestination in listOf("WORKER_JOBS", "DASHBOARD", "HOME"),
                        onClick = { activeNavDestination = "WORKER_JOBS" },
                        icon = { Icon(Icons.Default.Engineering, contentDescription = "Available Work") },
                        label = { Text("Available Work", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElegantLavender,
                            selectedTextColor = ElegantLavender,
                            indicatorColor = Color(0xFF44474E),
                            unselectedIconColor = ElegantTextSecondary,
                            unselectedTextColor = ElegantTextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = activeNavDestination == "WORKER_EARNINGS",
                        onClick = { activeNavDestination = "WORKER_EARNINGS" },
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Earnings") },
                        label = { Text("Earnings", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElegantLavender,
                            selectedTextColor = ElegantLavender,
                            indicatorColor = Color(0xFF44474E),
                            unselectedIconColor = ElegantTextSecondary,
                            unselectedTextColor = ElegantTextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = activeNavDestination == "WORKER_PROFILE",
                        onClick = { activeNavDestination = "WORKER_PROFILE" },
                        icon = { Icon(Icons.Default.Badge, contentDescription = "Passport") },
                        label = { Text("Passport", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElegantLavender,
                            selectedTextColor = ElegantLavender,
                            indicatorColor = Color(0xFF44474E),
                            unselectedIconColor = ElegantTextSecondary,
                            unselectedTextColor = ElegantTextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = activeNavDestination == "AI_ASSISTANT",
                        onClick = { activeNavDestination = "AI_ASSISTANT" },
                        icon = { Icon(Icons.Default.SmartToy, contentDescription = "AI Sahayak") },
                        label = { Text("AI Sahayak", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElegantLavender,
                            selectedTextColor = ElegantLavender,
                            indicatorColor = Color(0xFF44474E),
                            unselectedIconColor = ElegantTextSecondary,
                            unselectedTextColor = ElegantTextSecondary
                        )
                    )
                } else if (currentRole == "CUSTOMER") {
                    NavigationBarItem(
                        selected = activeNavDestination in listOf("CUSTOMER_WORKERS", "HOME", "DASHBOARD"),
                        onClick = { activeNavDestination = "CUSTOMER_WORKERS" },
                        icon = { Icon(Icons.Default.Handyman, contentDescription = "Available Workers") },
                        label = { Text("Available Workers", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElegantLavender,
                            selectedTextColor = ElegantLavender,
                            indicatorColor = Color(0xFF44474E),
                            unselectedIconColor = ElegantTextSecondary,
                            unselectedTextColor = ElegantTextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = activeNavDestination == "CUSTOMER_BOOKINGS",
                        onClick = { activeNavDestination = "CUSTOMER_BOOKINGS" },
                        icon = { Icon(Icons.Default.Assignment, contentDescription = "My Bookings") },
                        label = { Text("My Bookings", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElegantLavender,
                            selectedTextColor = ElegantLavender,
                            indicatorColor = Color(0xFF44474E),
                            unselectedIconColor = ElegantTextSecondary,
                            unselectedTextColor = ElegantTextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = activeNavDestination == "MAP",
                        onClick = { activeNavDestination = "MAP" },
                        icon = { Icon(Icons.Default.Map, contentDescription = "Live Map") },
                        label = { Text("Live Map", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElegantLavender,
                            selectedTextColor = ElegantLavender,
                            indicatorColor = Color(0xFF44474E),
                            unselectedIconColor = ElegantTextSecondary,
                            unselectedTextColor = ElegantTextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = activeNavDestination == "AI_ASSISTANT",
                        onClick = { activeNavDestination = "AI_ASSISTANT" },
                        icon = { Icon(Icons.Default.SmartToy, contentDescription = "AI Sahayak") },
                        label = { Text("AI Sahayak", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElegantLavender,
                            selectedTextColor = ElegantLavender,
                            indicatorColor = Color(0xFF44474E),
                            unselectedIconColor = ElegantTextSecondary,
                            unselectedTextColor = ElegantTextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = activeNavDestination == "INSTITUTION",
                        onClick = { activeNavDestination = "INSTITUTION" },
                        icon = { Icon(Icons.Default.Business, contentDescription = "Institutions") },
                        label = { Text("Institutions", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElegantLavender,
                            selectedTextColor = ElegantLavender,
                            indicatorColor = Color(0xFF44474E),
                            unselectedIconColor = ElegantTextSecondary,
                            unselectedTextColor = ElegantTextSecondary
                        )
                    )
                } else {
                    NavigationBarItem(
                        selected = activeNavDestination in listOf("DASHBOARD", "HOME"),
                        onClick = { activeNavDestination = "DASHBOARD" },
                        icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Console") },
                        label = { Text("Console", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElegantLavender,
                            selectedTextColor = ElegantLavender,
                            indicatorColor = Color(0xFF44474E),
                            unselectedIconColor = ElegantTextSecondary,
                            unselectedTextColor = ElegantTextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = activeNavDestination == "MAP",
                        onClick = { activeNavDestination = "MAP" },
                        icon = { Icon(Icons.Default.Map, contentDescription = "Map") },
                        label = { Text("Live Map", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElegantLavender,
                            selectedTextColor = ElegantLavender,
                            indicatorColor = Color(0xFF44474E),
                            unselectedIconColor = ElegantTextSecondary,
                            unselectedTextColor = ElegantTextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = activeNavDestination == "INSTITUTION",
                        onClick = { activeNavDestination = "INSTITUTION" },
                        icon = { Icon(Icons.Default.Business, contentDescription = "B2B") },
                        label = { Text("Institutions", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElegantLavender,
                            selectedTextColor = ElegantLavender,
                            indicatorColor = Color(0xFF44474E),
                            unselectedIconColor = ElegantTextSecondary,
                            unselectedTextColor = ElegantTextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeNavDestination) {
                "WORKER_JOBS", "WORKER_EARNINGS", "WORKER_PROFILE" -> {
                    val targetTab = when (activeNavDestination) {
                        "WORKER_EARNINGS" -> 1
                        "WORKER_PROFILE" -> 2
                        else -> 0
                    }
                    WorkerDashboardScreen(
                        workerProfile = currentWorkerProfile,
                        assignedBookings = workerBookings,
                        availableWork = availableWork,
                        welfareWallet = welfareWallet,
                        welfareTransactions = welfareTransactions,
                        language = currentLanguage,
                        initialTab = targetTab,
                        onToggleAvailability = { isAvail ->
                            coroutineScope.launch {
                                currentWorkerProfile?.let {
                                    repository.setWorkerAvailability(it.id, isAvail)
                                }
                                snackbarHostState.showSnackbar(if (isAvail) "Status: ONLINE" else "Status: OFFLINE")
                            }
                        },
                        onAcceptJob = { bookingId ->
                            coroutineScope.launch {
                                currentWorkerProfile?.let { w ->
                                    repository.acceptJob(bookingId, w.id, w.name)
                                    snackbarHostState.showSnackbar("Job accepted! View in 'My Active Jobs'.")
                                }
                            }
                        },
                        onUpdateJobStatus = { bookingId, status ->
                            coroutineScope.launch {
                                repository.updateBookingStatus(bookingId, status)
                                snackbarHostState.showSnackbar("Job status updated to $status")
                            }
                        },
                        onEditProfile = { showProfileDialog = true },
                        onLogout = {
                            isLoggedIn = false
                            authenticatedUser = null
                            showLoginPortal = true
                        }
                    )
                }

                "CUSTOMER_WORKERS", "CUSTOMER_BOOKINGS" -> {
                    CustomerHomeScreen(
                        currentUser = currentUser,
                        categories = categories,
                        allWorkers = allWorkers,
                        customerBookings = customerBookings,
                        invoices = DatabaseSeeder.getInitialInvoices(),
                        language = currentLanguage,
                        initialTab = if (activeNavDestination == "CUSTOMER_BOOKINGS") 1 else 0,
                        onOpenAiChat = { activeNavDestination = "AI_ASSISTANT" },
                        onSubmitWorkRequest = { cat, desc, urgency, isEmerg, name, phone, addr, city, date, time, worker, cost ->
                            coroutineScope.launch {
                                val assignedWorker = worker ?: allWorkers.find { it.primarySkill.equals(cat, ignoreCase = true) } ?: allWorkers.firstOrNull()
                                repository.createBooking(
                                    customerId = currentUser.id,
                                    customerName = name.ifBlank { currentUser.name },
                                    customerPhone = phone.ifBlank { currentUser.phone },
                                    workerId = assignedWorker?.id ?: "",
                                    workerName = assignedWorker?.name ?: "Pending Cooperative Assignment",
                                    cooperativeId = assignedWorker?.cooperativeId ?: "coop_patna_central",
                                    serviceCategory = cat,
                                    problemDescription = desc,
                                    urgency = urgency,
                                    isEmergency = isEmerg,
                                    locationAddress = addr,
                                    city = city.ifBlank { currentUser.city },
                                    scheduledDate = date,
                                    scheduledTime = time,
                                    laborCost = cost
                                )
                                snackbarHostState.showSnackbar("Work request submitted for $cat on $date ($time)!")
                            }
                        },
                        onBookWorker = { worker, cat, desc, urgency, isEmergency, loc, cost ->
                            coroutineScope.launch {
                                repository.createBooking(
                                    customerId = currentUser.id,
                                    customerName = currentUser.name,
                                    customerPhone = currentUser.phone,
                                    workerId = worker.id,
                                    workerName = worker.name,
                                    cooperativeId = worker.cooperativeId,
                                    serviceCategory = cat,
                                    problemDescription = desc,
                                    urgency = urgency,
                                    isEmergency = isEmergency,
                                    locationAddress = loc,
                                    city = currentUser.city,
                                    scheduledDate = "Today",
                                    scheduledTime = "Instant",
                                    laborCost = cost
                                )
                                snackbarHostState.showSnackbar("Booking confirmed with ${worker.name}!")
                            }
                        },
                        onMakeDemoPayment = { bId, method ->
                            coroutineScope.launch {
                                repository.processDemoPayment(bId, method)
                                snackbarHostState.showSnackbar("Payment confirmed ($method). Digital invoice generated!")
                            }
                        },
                        onSubmitRating = { bId, wId, rating, review ->
                            coroutineScope.launch {
                                repository.submitRating(bId, wId, currentUser.id, currentUser.name, rating, review)
                                snackbarHostState.showSnackbar("Thank you! Review & ★$rating rating submitted.")
                            }
                        },
                        onRaiseDispute = { bId, wId, reason, desc ->
                            coroutineScope.launch {
                                repository.raiseDispute(bId, currentUser.id, currentUser.name, wId, reason, desc)
                                snackbarHostState.showSnackbar("Dispute submitted to cooperative administration.")
                            }
                        },
                        onCancelBooking = { bId ->
                            coroutineScope.launch {
                                repository.updateBookingStatus(bId, "CANCELLED")
                                snackbarHostState.showSnackbar("Booking cancelled successfully.")
                            }
                        }
                    )
                }

                "HOME" -> {
                    if (currentRole == "WORKER") {
                        WorkerDashboardScreen(
                            workerProfile = currentWorkerProfile,
                            assignedBookings = workerBookings,
                            availableWork = availableWork,
                            welfareWallet = welfareWallet,
                            welfareTransactions = welfareTransactions,
                            language = currentLanguage,
                            initialTab = 0,
                            onToggleAvailability = { isAvail ->
                                coroutineScope.launch {
                                    currentWorkerProfile?.let {
                                        repository.setWorkerAvailability(it.id, isAvail)
                                    }
                                    snackbarHostState.showSnackbar(if (isAvail) "Status: ONLINE" else "Status: OFFLINE")
                                }
                            },
                            onAcceptJob = { bookingId ->
                                coroutineScope.launch {
                                    currentWorkerProfile?.let { w ->
                                        repository.acceptJob(bookingId, w.id, w.name)
                                        snackbarHostState.showSnackbar("Job accepted! View in 'My Active Jobs'.")
                                    }
                                }
                            },
                            onUpdateJobStatus = { bookingId, status ->
                                coroutineScope.launch {
                                    repository.updateBookingStatus(bookingId, status)
                                    snackbarHostState.showSnackbar("Job status updated to $status")
                                }
                            },
                            onLogout = {
                                isLoggedIn = false
                                authenticatedUser = null
                                showLoginPortal = true
                            }
                        )
                    } else {
                        CustomerHomeScreen(
                            currentUser = currentUser,
                            categories = categories,
                            allWorkers = allWorkers,
                            customerBookings = customerBookings,
                            invoices = DatabaseSeeder.getInitialInvoices(),
                            language = currentLanguage,
                            initialTab = 0,
                            onOpenAiChat = { activeNavDestination = "AI_ASSISTANT" },
                            onSubmitWorkRequest = { cat, desc, urgency, isEmerg, name, phone, addr, city, date, time, worker, cost ->
                                coroutineScope.launch {
                                    val assignedWorker = worker ?: allWorkers.find { it.primarySkill.equals(cat, ignoreCase = true) } ?: allWorkers.firstOrNull()
                                    repository.createBooking(
                                        customerId = currentUser.id,
                                        customerName = name.ifBlank { currentUser.name },
                                        customerPhone = phone.ifBlank { currentUser.phone },
                                        workerId = assignedWorker?.id ?: "",
                                        workerName = assignedWorker?.name ?: "Pending Cooperative Assignment",
                                        cooperativeId = assignedWorker?.cooperativeId ?: "coop_patna_central",
                                        serviceCategory = cat,
                                        problemDescription = desc,
                                        urgency = urgency,
                                        isEmergency = isEmerg,
                                        locationAddress = addr,
                                        city = city.ifBlank { currentUser.city },
                                        scheduledDate = date,
                                        scheduledTime = time,
                                        laborCost = cost
                                    )
                                    snackbarHostState.showSnackbar("Work request submitted for $cat on $date ($time)!")
                                }
                            },
                            onBookWorker = { worker, cat, desc, urgency, isEmergency, loc, cost ->
                                coroutineScope.launch {
                                    repository.createBooking(
                                        customerId = currentUser.id,
                                        customerName = currentUser.name,
                                        customerPhone = currentUser.phone,
                                        workerId = worker.id,
                                        workerName = worker.name,
                                        cooperativeId = worker.cooperativeId,
                                        serviceCategory = cat,
                                        problemDescription = desc,
                                        urgency = urgency,
                                        isEmergency = isEmergency,
                                        locationAddress = loc,
                                        city = currentUser.city,
                                        scheduledDate = "Today",
                                        scheduledTime = "Instant",
                                        laborCost = cost
                                    )
                                    snackbarHostState.showSnackbar("Booking confirmed with ${worker.name}!")
                                }
                            },
                            onMakeDemoPayment = { bId, method ->
                                coroutineScope.launch {
                                    repository.processDemoPayment(bId, method)
                                    snackbarHostState.showSnackbar("Payment confirmed ($method). Digital invoice generated!")
                                }
                            },
                            onSubmitRating = { bId, wId, rating, review ->
                                coroutineScope.launch {
                                    repository.submitRating(bId, wId, currentUser.id, currentUser.name, rating, review)
                                    snackbarHostState.showSnackbar("Thank you! Review & ★$rating rating submitted.")
                                }
                            },
                            onRaiseDispute = { bId, wId, reason, desc ->
                                coroutineScope.launch {
                                    repository.raiseDispute(bId, currentUser.id, currentUser.name, wId, reason, desc)
                                    snackbarHostState.showSnackbar("Dispute submitted to cooperative administration.")
                                }
                            }
                        )
                    }
                }

                "DASHBOARD" -> {
                    when (currentRole) {
                        "CUSTOMER" -> {
                            CustomerHomeScreen(
                                currentUser = currentUser,
                                categories = categories,
                                allWorkers = allWorkers,
                                customerBookings = customerBookings,
                                invoices = DatabaseSeeder.getInitialInvoices(),
                                language = currentLanguage,
                                onOpenAiChat = { activeNavDestination = "AI_ASSISTANT" },
                                onBookWorker = { worker, cat, desc, urgency, isEmergency, loc, cost ->
                                    coroutineScope.launch {
                                        repository.createBooking(
                                            customerId = currentUser.id,
                                            customerName = currentUser.name,
                                            customerPhone = currentUser.phone,
                                            workerId = worker.id,
                                            workerName = worker.name,
                                            cooperativeId = worker.cooperativeId,
                                            serviceCategory = cat,
                                            problemDescription = desc,
                                            urgency = urgency,
                                            isEmergency = isEmergency,
                                            locationAddress = loc,
                                            city = currentUser.city,
                                            scheduledDate = "Today",
                                            scheduledTime = "Instant",
                                            laborCost = cost
                                        )
                                        snackbarHostState.showSnackbar("Booking confirmed with ${worker.name}!")
                                    }
                                },
                                onMakeDemoPayment = { bId, method ->
                                    coroutineScope.launch {
                                        repository.processDemoPayment(bId, method)
                                        snackbarHostState.showSnackbar("Payment confirmed ($method). Digital invoice generated!")
                                    }
                                },
                                onSubmitRating = { bId, wId, rating, review ->
                                    coroutineScope.launch {
                                        repository.submitRating(bId, wId, currentUser.id, currentUser.name, rating, review)
                                        snackbarHostState.showSnackbar("Thank you! Review & ★$rating rating submitted.")
                                    }
                                },
                                onRaiseDispute = { bId, wId, reason, desc ->
                                    coroutineScope.launch {
                                        repository.raiseDispute(bId, currentUser.id, currentUser.name, wId, reason, desc)
                                        snackbarHostState.showSnackbar("Dispute submitted to cooperative administration.")
                                    }
                                }
                            )
                        }

                        "WORKER" -> {
                            WorkerDashboardScreen(
                                workerProfile = currentWorkerProfile,
                                assignedBookings = workerBookings,
                                availableWork = availableWork,
                                welfareWallet = welfareWallet,
                                welfareTransactions = welfareTransactions,
                                language = currentLanguage,
                                initialTab = 0,
                                onToggleAvailability = { isAvail ->
                                    coroutineScope.launch {
                                        currentWorkerProfile?.let {
                                            repository.setWorkerAvailability(it.id, isAvail)
                                        }
                                        snackbarHostState.showSnackbar(if (isAvail) "Status: ONLINE" else "Status: OFFLINE")
                                    }
                                },
                                onAcceptJob = { bookingId ->
                                    coroutineScope.launch {
                                        currentWorkerProfile?.let { w ->
                                            repository.acceptJob(bookingId, w.id, w.name)
                                            snackbarHostState.showSnackbar("Job accepted! View in 'My Active Jobs'.")
                                        }
                                    }
                                },
                                onUpdateJobStatus = { bookingId, status ->
                                    coroutineScope.launch {
                                        repository.updateBookingStatus(bookingId, status)
                                        snackbarHostState.showSnackbar("Job status updated to $status")
                                    }
                                },
                                onLogout = {
                                    isLoggedIn = false
                                    authenticatedUser = null
                                    showLoginPortal = true
                                }
                            )
                        }

                        "COOPERATIVE_ADMIN" -> {
                            CooperativeControlCenterScreen(
                                currentAdmin = currentUser,
                                cooperative = cooperatives.firstOrNull(),
                                allWorkers = allWorkers,
                                allBookings = allBookings,
                                allCooperatives = cooperatives,
                                demandForecasts = demandForecasts,
                                disputes = disputes,
                                language = currentLanguage,
                                onVerifyWorker = { workerId, status ->
                                    coroutineScope.launch {
                                        repository.setWorkerVerification(workerId, status)
                                        snackbarHostState.showSnackbar("Artisan skill verification marked $status")
                                    }
                                },
                                onAddWorker = { newWorker ->
                                    coroutineScope.launch {
                                        repository.registerWorker(newWorker)
                                        snackbarHostState.showSnackbar("New cooperative artisan registered!")
                                    }
                                },
                                onAssignWorkerToBooking = { bookingId, workerId, workerName ->
                                    coroutineScope.launch {
                                        // Update assigned worker
                                        repository.updateBookingStatus(bookingId, "ASSIGNED")
                                        snackbarHostState.showSnackbar("Assigned to $workerName")
                                    }
                                },
                                onResolveDispute = { disputeId, status, response ->
                                    coroutineScope.launch {
                                        // Resolve
                                        snackbarHostState.showSnackbar("Dispute resolved with official settlement.")
                                    }
                                }
                            )
                        }

                        "FEDERATION_ADMIN", "SUPER_ADMIN" -> {
                            FederationDashboardScreen(
                                currentAdmin = currentUser,
                                cooperatives = cooperatives,
                                allWorkers = allWorkers,
                                allBookings = allBookings,
                                demandForecasts = demandForecasts,
                                language = currentLanguage
                            )
                        }
                    }
                }

                "MAP" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ElegantDarkBg)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Live Cooperative Artisan Map",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = ElegantTextWhite
                        )
                        Text(
                            text = "Track live worker availability, emergency dispatch units, and cooperative branches.",
                            fontSize = 12.sp,
                            color = ElegantTextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LiveWorkerMap(
                            workers = allWorkers,
                            cooperatives = cooperatives,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                "INSTITUTION" -> {
                    InstitutionalMarketplaceScreen(
                        institutionalBookings = institutionalBookings,
                        cooperatives = cooperatives,
                        language = currentLanguage,
                        onSubmitRequest = { name, type, skill, count, days, city, person, phone, coopId ->
                            coroutineScope.launch {
                                repository.submitInstitutionalRequest(
                                    institutionName = name,
                                    institutionType = type,
                                    requiredSkill = skill,
                                    workerCount = count,
                                    durationDays = days,
                                    locationCity = city,
                                    contactPerson = person,
                                    contactPhone = phone,
                                    cooperativeId = coopId
                                )
                                snackbarHostState.showSnackbar("Bulk request submitted to cooperative!")
                            }
                        }
                    )
                }

                "AI_ASSISTANT" -> {
                    GeminiChatScreen(
                        onNavigateBack = {
                            activeNavDestination = if (currentRole == "WORKER") "WORKER_JOBS" else "CUSTOMER_WORKERS"
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    // Notification Center Dialog
    if (showNotificationDialog) {
        NotificationCenterDialog(
            notifications = userNotifications,
            onDismiss = { showNotificationDialog = false },
            onMarkAsRead = { notifId ->
                coroutineScope.launch {
                    repository.markNotificationAsRead(notifId)
                }
            },
            onMarkAllAsRead = {
                coroutineScope.launch {
                    repository.markAllNotificationsAsRead(currentUser.id, currentUser.role)
                }
            }
        )
    }

    // User Profile Dialog
    if (showProfileDialog) {
        UserProfileDialog(
            user = currentUser,
            workerProfile = currentWorkerProfile,
            onDismiss = { showProfileDialog = false },
            onSaveWorkerProfile = { name, email, phone, city, state, district, village,
                                   primarySkill, secondarySkills, experienceYears, certifications,
                                   dailyWageRate, isAvailable, isEmergencyReady, zone ->
                coroutineScope.launch {
                    repository.updateWorkerProfessionalProfile(
                        userId = currentUser.id,
                        name = name,
                        email = email,
                        phone = phone,
                        city = city,
                        state = state,
                        district = district,
                        village = village,
                        primarySkill = primarySkill,
                        secondarySkills = secondarySkills,
                        experienceYears = experienceYears,
                        certifications = certifications,
                        dailyWageRate = dailyWageRate,
                        isAvailable = isAvailable,
                        isEmergencyReady = isEmergencyReady,
                        zone = zone
                    )
                    authenticatedUser = currentUser.copy(
                        name = name,
                        email = email,
                        phone = phone,
                        city = city,
                        state = state,
                        district = district,
                        village = village
                    )
                    snackbarHostState.showSnackbar("Worker professional profile & skills updated successfully!")
                }
            },
            onSaveCustomerProfile = { name, email, phone, city, state, district, village ->
                coroutineScope.launch {
                    repository.updateCustomerProfile(
                        userId = currentUser.id,
                        name = name,
                        email = email,
                        phone = phone,
                        city = city,
                        state = state,
                        district = district,
                        village = village
                    )
                    authenticatedUser = currentUser.copy(
                        name = name,
                        email = email,
                        phone = phone,
                        city = city,
                        state = state,
                        district = district,
                        village = village
                    )
                    snackbarHostState.showSnackbar("Customer profile & contact information updated successfully!")
                }
            },
            onUpdateProfile = { name, email, state, district, village ->
                coroutineScope.launch {
                    repository.updateUserProfile(currentUser.id, name, email, state, district, village)
                    authenticatedUser = currentUser.copy(
                        name = name,
                        email = email,
                        state = state,
                        district = district,
                        village = village
                    )
                    snackbarHostState.showSnackbar("Profile updated successfully.")
                }
            },
            onChangePassword = { oldPlain, newPlain ->
                coroutineScope.launch {
                    val (ok, message) = repository.changePassword(currentUser.id, oldPlain, newPlain)
                    snackbarHostState.showSnackbar(message)
                }
            },
            onLogout = {
                isLoggedIn = false
                authenticatedUser = null
                showProfileDialog = false
                showLoginPortal = true
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Logged out successfully.")
                }
            }
        )
    }
    }
    }
}
