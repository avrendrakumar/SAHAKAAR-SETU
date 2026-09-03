package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.LanguageManager
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CooperativeDashboardScreen(
    cooperatives: List<CooperativeEntity>,
    workers: List<WorkerProfileEntity>,
    bookings: List<BookingEntity>,
    disputes: List<DisputeEntity>,
    demandForecasts: List<DemandForecastEntity>,
    institutionalBookings: List<InstitutionalBookingEntity>,
    onOpenPassport: (WorkerProfileEntity) -> Unit,
    onVerifyWorker: (WorkerProfileEntity) -> Unit,
    onToggleSuspend: (WorkerProfileEntity) -> Unit,
    onAssignWorkerToBooking: (bookingId: Long, worker: WorkerProfileEntity) -> Unit,
    onAddNewWorker: (name: String, phone: String, skill: String, coopId: Long, coopName: String, exp: Int, rate: Double) -> Unit,
    onOpenLiveMap: () -> Unit
) {
    var selectedAdminTab by remember { mutableStateOf(0) }
    var showAddWorkerDialog by remember { mutableStateOf(false) }
    var workerSearchQuery by remember { mutableStateOf("") }
    var workerSkillFilter by remember { mutableStateOf("All") }
    var selectedJobForAssign by remember { mutableStateOf<BookingEntity?>(null) }
    val isHi = LanguageManager.isHindi

    val tabs = listOf(
        Pair("Workers", Icons.Default.Engineering),
        Pair("Jobs & Dispatch", Icons.Default.Assignment),
        Pair("Live Map", Icons.Default.Map),
        Pair("AI Forecasting", Icons.Default.TrendingUp),
        Pair("Institutional", Icons.Default.Business),
        Pair("Disputes", Icons.Default.Gavel)
    )

    // Primary Cooperative (Patna Shramik Sahakari Samiti)
    val currentCoop = cooperatives.firstOrNull() ?: CooperativeEntity(
        id = 1,
        name = "Patna Shramik Sahakari Samiti",
        registrationNumber = "COOP-BR-PAT-2024-001",
        district = "Patna",
        address = "Fraser Road, Patna",
        phone = "+91 612 220918",
        email = "patna@sahakaar.coop.in",
        latitude = 25.6093,
        longitude = 85.1376
    )

    // Calculate all 11 KPIs
    val totalWorkersCount = workers.size
    val verifiedWorkersCount = workers.count { it.isVerified }
    val availableWorkersCount = workers.count { it.isOnline && !it.isBusy }
    val busyWorkersCount = workers.count { it.isBusy }
    val jobsTodayCount = bookings.size
    val pendingJobsCount = bookings.count { it.status == BookingStatus.PENDING || it.status == BookingStatus.ASSIGNED }
    val completedJobsCount = bookings.count { it.status == BookingStatus.COMPLETED }
    val emergencyJobsCount = bookings.count { it.isEmergency }
    val totalRevenue = bookings.filter { it.status == BookingStatus.COMPLETED }.sumOf { it.totalAmount }
    val utilisationPercent = if (totalWorkersCount > 0) ((busyWorkersCount.toDouble() / totalWorkersCount) * 100).toInt() else 0
    val avgRating = if (workers.isNotEmpty()) ((workers.map { it.rating }.average() * 10).toInt() / 10.0) else 4.8

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
    ) {
        // Top Cooperative Banner
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentCoop.name,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = Slate900
                        )
                        Text(
                            text = "District: ${currentCoop.district} • Reg: COOP-BR-2024-001 • Contact: ${currentCoop.phone}",
                            fontSize = 11.sp,
                            color = Slate600
                        )
                    }

                    Button(
                        onClick = { showAddWorkerDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SahakaarNavy),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isHi) "श्रमिक जोड़ें" else "+ Enroll Worker", fontSize = 11.5.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Control Center 11-KPI Strip (Horizontally scrollable)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminKpiPill("Total Workers", "$totalWorkersCount", SahakaarNavy)
                    AdminKpiPill("Verified", "$verifiedWorkersCount", SahakaarTeal)
                    AdminKpiPill("Available", "$availableWorkersCount", SahakaarGreen)
                    AdminKpiPill("Busy on Job", "$busyWorkersCount", SahakaarAmber)
                    AdminKpiPill("Jobs Today", "$jobsTodayCount", SahakaarBlue)
                    AdminKpiPill("Pending Dispatch", "$pendingJobsCount", SahakaarCrimson)
                    AdminKpiPill("Completed", "$completedJobsCount", SahakaarGreen)
                    AdminKpiPill("🚨 Emergency", "$emergencyJobsCount", SahakaarCrimson)
                    AdminKpiPill("Total Revenue", "₹${totalRevenue.toInt()}", SahakaarTeal)
                    AdminKpiPill("Utilisation", "$utilisationPercent%", SahakaarNavy)
                    AdminKpiPill("Avg Rating", "$avgRating ★", SahakaarAmber)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedAdminTab,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    contentColor = SahakaarNavy
                ) {
                    tabs.forEachIndexed { index, (label, icon) ->
                        Tab(
                            selected = selectedAdminTab == index,
                            onClick = {
                                if (index == 2) {
                                    onOpenLiveMap()
                                } else {
                                    selectedAdminTab = index
                                }
                            },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(label, fontSize = 12.sp, fontWeight = if (selectedAdminTab == index) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedAdminTab) {
                0 -> WorkersManagementTab(
                    workers = workers,
                    searchQuery = workerSearchQuery,
                    onSearchQueryChange = { workerSearchQuery = it },
                    skillFilter = workerSkillFilter,
                    onSkillFilterChange = { workerSkillFilter = it },
                    onOpenPassport = onOpenPassport,
                    onVerifyWorker = onVerifyWorker,
                    onToggleSuspend = onToggleSuspend
                )
                1 -> JobManagementTab(
                    bookings = bookings,
                    workers = workers,
                    onSelectJobForAssign = { selectedJobForAssign = it }
                )
                2 -> {
                    // Live Map opens full view
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Button(onClick = onOpenLiveMap) {
                            Text("Open Live GIS Map")
                        }
                    }
                }
                3 -> DemandForecastingTab(forecasts = demandForecasts)
                4 -> InstitutionalTab(bookings = institutionalBookings)
                5 -> DisputesTab(disputes = disputes)
            }
        }
    }

    // Modal to Enroll New Worker
    if (showAddWorkerDialog) {
        var newName by remember { mutableStateOf("") }
        var newPhone by remember { mutableStateOf("") }
        var newSkill by remember { mutableStateOf("Plumbing") }
        var newExp by remember { mutableStateOf("4") }
        var newRate by remember { mutableStateOf("350") }

        AlertDialog(
            onDismissRequest = { showAddWorkerDialog = false },
            title = { Text(if (isHi) "नया सहकारी श्रमिक पंजीकृत करें" else "Enroll New Cooperative Worker", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Worker Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Phone (+91)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newSkill,
                        onValueChange = { newSkill = it },
                        label = { Text("Primary Skill (Plumbing, Electrical, etc.)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = newExp,
                            onValueChange = { newExp = it },
                            label = { Text("Exp (Years)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newRate,
                            onValueChange = { newRate = it },
                            label = { Text("Base Rate (₹)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            onAddNewWorker(
                                newName,
                                newPhone.ifBlank { "+91 94310 99000" },
                                newSkill,
                                currentCoop.id,
                                currentCoop.name,
                                newExp.toIntOrNull() ?: 3,
                                newRate.toDoubleOrNull() ?: 350.0
                            )
                            showAddWorkerDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SahakaarNavy)
                ) {
                    Text("Enroll Worker")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddWorkerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal to Assign / Reassign Worker to a Job
    if (selectedJobForAssign != null) {
        val job = selectedJobForAssign!!
        AlertDialog(
            onDismissRequest = { selectedJobForAssign = null },
            title = { Text("Assign Worker to Job ${job.bookingCode}", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                ) {
                    Text("Service: ${job.serviceName} at ${job.address}", fontSize = 12.sp, color = Slate600)
                    Spacer(modifier = Modifier.height(10.dp))
                    val candidates = workers.filter { it.isOnline && (it.primarySkill == job.serviceName || it.secondarySkills.contains(job.serviceName)) }
                    if (candidates.isEmpty()) {
                        Text("No online workers found with matching skill. Showing all available workers:", fontSize = 11.sp, color = Slate500)
                    }
                    val availableToPick = if (candidates.isNotEmpty()) candidates else workers.filter { it.isOnline }

                    availableToPick.take(6).forEach { w ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Slate100),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable {
                                    onAssignWorkerToBooking(job.id, w)
                                    selectedJobForAssign = null
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(w.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${w.primarySkill} • ⭐ ${w.rating}", fontSize = 11.sp, color = Slate600)
                                }
                                Text("Assign →", color = SahakaarNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedJobForAssign = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun AdminKpiPill(title: String, value: String, color: Color) {
    Surface(
        color = Slate100,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text(title, fontSize = 10.sp, color = Slate600)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
private fun WorkersManagementTab(
    workers: List<WorkerProfileEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    skillFilter: String,
    onSkillFilterChange: (String) -> Unit,
    onOpenPassport: (WorkerProfileEntity) -> Unit,
    onVerifyWorker: (WorkerProfileEntity) -> Unit,
    onToggleSuspend: (WorkerProfileEntity) -> Unit
) {
    val filtered = workers.filter { w ->
        val qMatch = w.name.contains(searchQuery, ignoreCase = true) || w.workerCode.contains(searchQuery, ignoreCase = true)
        val sMatch = if (skillFilter == "All") true else w.primarySkill.equals(skillFilter, ignoreCase = true)
        qMatch && sMatch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Search and Filter Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search worker by name or ID...", fontSize = 12.5.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate500) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("All", "Plumbing", "Electrical", "Carpentry", "Painting", "Masonry", "AC Repair").forEach { skill ->
                FilterChip(
                    selected = skillFilter == skill,
                    onClick = { onSkillFilterChange(skill) },
                    label = { Text(skill, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Registered Workers (${filtered.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate800)
        Spacer(modifier = Modifier.height(6.dp))

        filtered.forEach { w ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(SahakaarNavy, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(w.name.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(w.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
                                Spacer(modifier = Modifier.width(4.dp))
                                if (w.isVerified) {
                                    Icon(Icons.Default.Verified, contentDescription = "Verified", tint = SahakaarTeal, modifier = Modifier.size(14.dp))
                                }
                            }
                            Text("ID: ${w.workerCode} • ${w.primarySkill} • Tel: ${w.phone}", fontSize = 11.sp, color = Slate600)
                        }
                        // Online / Offline Status Dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(if (w.isOnline) SahakaarGreen else Slate400, CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⭐ ${w.rating} (${w.reviewCount}) • Reliability: ${w.reliabilityScore}% • Jobs: ${w.completedJobs}",
                            fontSize = 11.sp,
                            color = Slate600
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Passport action
                            OutlinedButton(
                                onClick = { onOpenPassport(w) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Passport & QR", fontSize = 11.sp)
                            }

                            // Suspend/Activate
                            OutlinedButton(
                                onClick = { onToggleSuspend(w) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(if (w.isOnline) "Suspend" else "Activate", fontSize = 11.sp, color = if (w.isOnline) SahakaarCrimson else SahakaarGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JobManagementTab(
    bookings: List<BookingEntity>,
    workers: List<WorkerProfileEntity>,
    onSelectJobForAssign: (BookingEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Cooperative Job Dispatch Board (${bookings.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
        Spacer(modifier = Modifier.height(10.dp))

        bookings.forEach { b ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (b.isEmergency) {
                                Surface(color = SahakaarCrimson, shape = RoundedCornerShape(4.dp)) {
                                    Text("🚨 EMERGENCY", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(b.bookingCode, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Slate900)
                        }

                        Surface(
                            color = when (b.status) {
                                BookingStatus.COMPLETED -> SahakaarGreen.copy(alpha = 0.15f)
                                BookingStatus.IN_PROGRESS -> SahakaarAmber.copy(alpha = 0.2f)
                                BookingStatus.ON_THE_WAY -> SahakaarBlue.copy(alpha = 0.15f)
                                else -> Slate200
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = b.status.displayName,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("${b.serviceName} — ${b.problemDescription}", fontSize = 12.sp, color = Slate700)
                    Text("Customer: ${b.customerName} (${b.address})", fontSize = 11.sp, color = Slate600)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Assigned Worker: ${b.workerName ?: "Unassigned"}", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = if (b.workerName == null) SahakaarCrimson else SahakaarNavy)
                        Button(
                            onClick = { onSelectJobForAssign(b) },
                            colors = ButtonDefaults.buttonColors(containerColor = SahakaarNavy),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(if (b.workerName == null) "Assign Worker" else "Reassign", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DemandForecastingTab(forecasts: List<DemandForecastEntity>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("AI Workforce Demand Intelligence", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Slate900)
        Text("Predictive algorithms identifying worker shortages & surges across Bihar districts", fontSize = 11.5.sp, color = Slate600)

        Spacer(modifier = Modifier.height(14.dp))

        forecasts.forEach { f ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${f.serviceName} (${f.zone})", fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = Slate900)
                        Surface(
                            color = if (f.recommendation.contains("Surge", ignoreCase = true) || f.predictedDemand > f.availableWorkers) Color(0xFFFEE2E2) else PrimaryContainerLight,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (f.predictedDemand > f.availableWorkers) "DEFICIT WARNING" else "BALANCED",
                                color = if (f.predictedDemand > f.availableWorkers) SahakaarCrimson else SahakaarTeal,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Predicted Demand", fontSize = 10.5.sp, color = Slate500)
                            Text("${f.predictedDemand} jobs", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SahakaarBlue)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Available Supply", fontSize = 10.5.sp, color = Slate500)
                            Text("${f.availableWorkers} workers", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SahakaarGreen)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Net Gap", fontSize = 10.5.sp, color = Slate500)
                            val diff = f.predictedDemand - f.availableWorkers
                            Text(
                                text = if (diff > 0) "-$diff (Shortage)" else "+${-diff} (Surplus)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (diff > 0) SahakaarCrimson else SahakaarGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("AI Recommendation: ${f.recommendation}", fontSize = 11.5.sp, color = Slate700)
                }
            }
        }
    }
}

@Composable
private fun InstitutionalTab(bookings: List<InstitutionalBookingEntity>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Institutional & Bulk Workforce Marketplace", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Slate900)
        Text("Bulk contract labour deployments for Schools, Hospitals, Factories, Offices & Hoteliers", fontSize = 11.5.sp, color = Slate600)

        Spacer(modifier = Modifier.height(12.dp))

        bookings.forEach { ib ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(ib.institutionName, fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = Slate900)
                        Surface(color = PrimaryContainerLight, shape = RoundedCornerShape(6.dp)) {
                            Text(ib.status, color = SahakaarNavy, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Text("Type: ${ib.institutionType} • Trade: ${ib.tradeRequired}", fontSize = 11.5.sp, color = Slate600)
                    Text("Requirement: ${ib.workerCount} Workers for ${ib.durationDays} Days at ${ib.location}", fontSize = 11.5.sp, color = Slate700)
                    Text("Budget: ₹${ib.estimatedBudget.toInt()} • Allocated: ${ib.cooperativeAllocated}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SahakaarTeal)
                }
            }
        }
    }
}

@Composable
private fun DisputesTab(disputes: List<DisputeEntity>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Cooperative Dispute Resolution Board (${disputes.size})", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Slate900)
        Text("Protecting both customers and craftsmen with transparent cooperative mediation", fontSize = 11.5.sp, color = Slate600)

        Spacer(modifier = Modifier.height(12.dp))

        if (disputes.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) {
                Text("Zero active disputes. Outstanding cooperative satisfaction!", color = Slate500)
            }
        } else {
            disputes.forEach { d ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Dispute for ${d.bookingCode}", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = SahakaarCrimson)
                            Surface(color = Color(0xFFFEE2E2), shape = RoundedCornerShape(4.dp)) {
                                Text(d.status, color = SahakaarCrimson, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Reason: ${d.reason}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate800)
                        Text(d.description, fontSize = 11.5.sp, color = Slate600)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Customer: ${d.customerName} • Craftsman: ${d.workerName}", fontSize = 11.sp, color = Slate500)
                    }
                }
            }
        }
    }
}
