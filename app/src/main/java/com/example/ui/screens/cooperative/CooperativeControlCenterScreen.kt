package com.example.ui.screens.cooperative

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.components.AvailabilityIndicator
import com.example.ui.components.BookingStatusChip
import com.example.ui.components.LiveWorkerMap
import com.example.ui.components.SkillPassportCard
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CooperativeControlCenterScreen(
    currentAdmin: UserEntity,
    cooperative: CooperativeEntity?,
    allWorkers: List<WorkerProfileEntity>,
    allBookings: List<BookingEntity>,
    allCooperatives: List<CooperativeEntity>,
    demandForecasts: List<DemandForecastEntity>,
    disputes: List<DisputeEntity>,
    language: AppLanguage,
    onVerifyWorker: (String, String) -> Unit,
    onAddWorker: (WorkerProfileEntity) -> Unit,
    onAssignWorkerToBooking: (String, String, String) -> Unit,
    onResolveDispute: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableIntStateOf(0) } // 0: Overview KPIs, 1: Live Map, 2: Workers, 3: Bookings, 4: Demand Intelligence, 5: Disputes

    var showAddWorkerDialog by remember { mutableStateOf(false) }
    var selectedWorkerForPassport by remember { mutableStateOf<WorkerProfileEntity?>(null) }
    var selectedBookingForAssignment by remember { mutableStateOf<BookingEntity?>(null) }
    var selectedDisputeForResolution by remember { mutableStateOf<DisputeEntity?>(null) }

    // Compute live real metrics from DB
    val totalWorkersCount = allWorkers.size
    val verifiedCount = allWorkers.count { it.verificationStatus == "VERIFIED" }
    val availableCount = allWorkers.count { it.isAvailable && !it.isBusy }
    val busyCount = allWorkers.count { it.isBusy }
    val emergencyCount = allBookings.count { it.isEmergency }
    val jobsTodayCount = allBookings.size
    val pendingJobsCount = allBookings.count { it.status in listOf("CREATED", "ASSIGNED", "ON_THE_WAY") }
    val completedJobsCount = allBookings.count { it.status == "COMPLETED" }
    val totalRevenue = allBookings.sumOf { it.totalAmount }
    val utilizationRate = if (totalWorkersCount > 0) ((busyCount.toDouble() / totalWorkersCount.toDouble()) * 100).toInt() else 0
    val avgRating = if (allWorkers.isNotEmpty()) (allWorkers.map { it.customerRating }.average() * 10).toInt() / 10.0 else 4.8

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ElegantDarkBg)
    ) {
        // Control Centre Top Banner
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin", tint = ElegantLavender, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppStrings.get("cooperative_control", language),
                                color = ElegantTextWhite,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }
                        Text(
                            text = cooperative?.name ?: "Patna Shramik Vikas Sahakari Samiti",
                            color = ElegantLavender,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Admin: ${currentAdmin.name} • Reg: ${cooperative?.registrationNumber ?: "COOP-BR-PAT"}",
                            color = ElegantTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { showAddWorkerDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantLavender,
                            contentColor = ElegantOnLavender
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Artisan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Horizontal Nav Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElegantDarkBg)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val sections = listOf(
                "Overview" to Icons.Default.Dashboard,
                "Live Map" to Icons.Default.Map,
                "Workers ($totalWorkersCount)" to Icons.Default.Engineering,
                "Jobs ($jobsTodayCount)" to Icons.AutoMirrored.Filled.Assignment,
                "Workforce AI" to Icons.Default.Analytics,
                "Disputes (${disputes.size})" to Icons.Default.Gavel
            )

            sections.forEachIndexed { index, (label, icon) ->
                FilterChip(
                    selected = selectedSection == index,
                    onClick = { selectedSection = index },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElegantLavender,
                        selectedLabelColor = ElegantOnLavender,
                        selectedLeadingIconColor = ElegantOnLavender,
                        containerColor = ElegantDarkSurface,
                        labelColor = ElegantTextSecondary,
                        iconColor = ElegantTextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedSection == index,
                        borderColor = ElegantDarkBorder,
                        selectedBorderColor = ElegantLavender
                    ),
                    shape = RoundedCornerShape(20.dp),
                    leadingIcon = { Icon(icon, contentDescription = label, modifier = Modifier.size(14.dp)) },
                    label = { Text(label, fontSize = 12.sp, fontWeight = if (selectedSection == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        // Active Section Content
        when (selectedSection) {
            0 -> {
                // Overview KPIs Grid
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("Cooperative Core Performance", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ElegantTextWhite)
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            KpiMetricCard("Total Artisans", "$totalWorkersCount", "$verifiedCount Verified", ElegantLavender, Modifier.weight(1f))
                            KpiMetricCard("Available Now", "$availableCount", "$busyCount On Duty", ElegantMintGreen, Modifier.weight(1f))
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            KpiMetricCard("Jobs Recorded", "$jobsTodayCount", "$completedJobsCount Completed", ElegantLavender, Modifier.weight(1f))
                            KpiMetricCard("Pending Jobs", "$pendingJobsCount", "$emergencyCount Emergency", Color(0xFFFFD54F), Modifier.weight(1f))
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            KpiMetricCard("Total Revenue", "₹${totalRevenue.toInt()}", "Direct to Workers", ElegantMintGreen, Modifier.weight(1f))
                            KpiMetricCard("Worker Utilisation", "$utilizationRate%", "Avg Rating: ★ $avgRating", ElegantLavender, Modifier.weight(1f))
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Live Operational Map Preview", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ElegantTextWhite)
                    }

                    item {
                        LiveWorkerMap(
                            workers = allWorkers,
                            cooperatives = allCooperatives,
                            onWorkerSelected = { selectedWorkerForPassport = it }
                        )
                    }
                }
            }

            1 -> {
                // Full Interactive Live Map
                Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    Text("Interactive Real-time Worker & Demand Heatmap", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LiveWorkerMap(
                        workers = allWorkers,
                        cooperatives = allCooperatives,
                        onWorkerSelected = { selectedWorkerForPassport = it },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            2 -> {
                // Workers Management
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Registered Cooperative Workers", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ElegantTextWhite)
                            Text("$totalWorkersCount Artisans", fontSize = 12.sp, color = ElegantTextSecondary)
                        }
                    }

                    if (allWorkers.isEmpty()) {
                        item {
                            Text(
                                text = "No registered workers in this cooperative yet. Tap 'Register Worker' above to onboard members.",
                                fontSize = 12.sp,
                                color = ElegantTextSecondary,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    } else {
                        items(allWorkers) { worker ->
                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(worker.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ElegantTextWhite)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            VerificationBadge(status = worker.verificationStatus)
                                        }
                                        Text("${worker.primarySkill} • ${worker.workerIdTag}", fontSize = 11.sp, color = ElegantTextSecondary)
                                    }

                                    AvailabilityIndicator(isAvailable = worker.isAvailable, isBusy = worker.isBusy)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Certifications: ${worker.certifications}",
                                    fontSize = 11.sp,
                                    color = ElegantTextSecondary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Rate: ₹${worker.dailyWageRate.toInt()}/day • Jobs: ${worker.completedJobs}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ElegantTextWhite
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedButton(
                                            onClick = { selectedWorkerForPassport = worker },
                                            shape = RoundedCornerShape(20.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("Passport", fontSize = 11.sp, color = ElegantLavender)
                                        }

                                        if (worker.verificationStatus != "VERIFIED") {
                                            Button(
                                                onClick = { onVerifyWorker(worker.id, "VERIFIED") },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = ElegantMintGreen,
                                                    contentColor = ElegantOnMintGreen
                                                ),
                                                shape = RoundedCornerShape(20.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text("Verify", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }

            3 -> {
                // Jobs / Bookings Management
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("Cooperative Job Dispatch & Allocation", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ElegantTextWhite)
                    }

                    if (allBookings.isEmpty()) {
                        item {
                            Text(
                                text = "No active job bookings recorded for this cooperative yet.",
                                fontSize = 12.sp,
                                color = ElegantTextSecondary,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    } else {
                        items(allBookings) { booking ->
                            Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Booking #${booking.bookingNumber}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ElegantTextWhite)
                                        Text("${booking.serviceCategory} • ${booking.city}", fontSize = 11.sp, color = ElegantTextSecondary)
                                    }
                                    BookingStatusChip(status = booking.status)
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Customer: ${booking.customerName} (${booking.customerPhone})", fontSize = 12.sp, color = ElegantTextWhite)
                                Text("Assigned Worker: ${booking.workerName}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ElegantLavender)
                                Text("Address: ${booking.locationAddress}", fontSize = 11.sp, color = ElegantTextSecondary)

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("₹${booking.totalAmount.toInt()} (${booking.paymentStatus})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ElegantMintGreen)

                                    if (booking.status in listOf("CREATED", "ASSIGNED")) {
                                        Button(
                                            onClick = { selectedBookingForAssignment = booking },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ElegantLavender,
                                                contentColor = ElegantOnLavender
                                            ),
                                            shape = RoundedCornerShape(20.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("Reassign", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }

            4 -> {
                // Workforce Intelligence / Demand Forecasts
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Insights, contentDescription = "AI", tint = ElegantLavender, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Workforce Intelligence & Shortage Forecaster", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ElegantTextWhite)
                            }
                            Text("Predictive analytics based on seasonal demand, weather & construction surges.", fontSize = 11.sp, color = ElegantTextSecondary)
                        }
                    }

                    if (demandForecasts.isEmpty()) {
                        item {
                            Text(
                                text = "No predictive forecasts generated yet.",
                                fontSize = 12.sp,
                                color = ElegantTextSecondary,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    } else {
                        items(demandForecasts) { forecast ->
                            Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${forecast.serviceCategory} • ${forecast.district}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = ElegantTextWhite
                                    )
                                    Surface(
                                        color = if (forecast.shortage > 50) Color(0xFF3B1E22) else Color(0xFF3E2E10),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (forecast.shortage > 50) ElegantEmergencyRed.copy(alpha = 0.4f) else Color(0xFFFFD54F).copy(alpha = 0.4f)
                                        )
                                    ) {
                                        Text(
                                            text = "Shortage: -${forecast.shortage}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (forecast.shortage > 50) ElegantEmergencyRed else Color(0xFFFFD54F),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Predicted Demand: ${forecast.predictedDemand} requests", fontSize = 11.sp, color = ElegantTextSecondary)
                                    Text("Available Artisans: ${forecast.availableWorkers}", fontSize = 11.sp, color = ElegantTextSecondary)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Surface(
                                    color = ElegantDarkSurfaceVariant,
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(Icons.Default.Lightbulb, contentDescription = "Rec", tint = ElegantLavender, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = forecast.recommendation,
                                            fontSize = 11.sp,
                                            color = ElegantTextWhite,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }

            5 -> {
                // Disputes Center
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("Customer & Worker Disputes", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ElegantTextWhite)
                    }

                    if (disputes.isEmpty()) {
                        item {
                            Text("No unresolved disputes in this cooperative jurisdiction.", fontSize = 12.sp, color = ElegantTextSecondary)
                        }
                    } else {
                        items(disputes) { dispute ->
                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Dispute: ${dispute.reason}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ElegantEmergencyRed)
                                        Text(dispute.status, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = ElegantLavender)
                                    }
                                    Text("Customer: ${dispute.customerName}", fontSize = 12.sp, color = ElegantTextWhite)
                                    Text("Description: ${dispute.description}", fontSize = 11.sp, color = ElegantTextSecondary)

                                    if (dispute.adminResponse != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Co-op Resolution: ${dispute.adminResponse}", fontSize = 11.sp, color = ElegantMintGreen, fontWeight = FontWeight.SemiBold)
                                    } else {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = { selectedDisputeForResolution = dispute },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ElegantLavender,
                                                contentColor = ElegantOnLavender
                                            ),
                                            shape = RoundedCornerShape(20.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Resolve Dispute", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }
        }

    // Add Worker Dialog
    if (showAddWorkerDialog) {
        var wName by remember { mutableStateOf("") }
        var wPhone by remember { mutableStateOf("") }
        var wSkill by remember { mutableStateOf("Electrical") }
        var wExp by remember { mutableStateOf("5") }
        var wCert by remember { mutableStateOf("ITI Diploma / NSDC Certified") }
        var wRate by remember { mutableStateOf("450") }

        Dialog(onDismissRequest = { showAddWorkerDialog = false }) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Register New Cooperative Artisan", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = ElegantTextWhite)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = wName,
                        onValueChange = { wName = it },
                        label = { Text("Worker Full Name") },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ElegantTextWhite, unfocusedTextColor = ElegantTextWhite,
                            focusedBorderColor = ElegantLavender, unfocusedBorderColor = ElegantDarkBorder,
                            focusedContainerColor = ElegantDarkSurfaceVariant, unfocusedContainerColor = ElegantDarkSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = wPhone,
                        onValueChange = { wPhone = it },
                        label = { Text("Phone Number") },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ElegantTextWhite, unfocusedTextColor = ElegantTextWhite,
                            focusedBorderColor = ElegantLavender, unfocusedBorderColor = ElegantDarkBorder,
                            focusedContainerColor = ElegantDarkSurfaceVariant, unfocusedContainerColor = ElegantDarkSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Trade:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ElegantTextWhite)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Electrical", "Plumbing", "Carpentry", "Masonry", "Painting", "AC Repair").forEach { trade ->
                            FilterChip(
                                selected = wSkill == trade,
                                onClick = { wSkill = trade },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElegantLavender,
                                    selectedLabelColor = ElegantOnLavender,
                                    containerColor = ElegantDarkSurfaceVariant,
                                    labelColor = ElegantTextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = wSkill == trade,
                                    borderColor = ElegantDarkBorder,
                                    selectedBorderColor = ElegantLavender
                                ),
                                label = { Text(trade, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = wExp,
                        onValueChange = { wExp = it },
                        label = { Text("Experience (Years)") },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ElegantTextWhite, unfocusedTextColor = ElegantTextWhite,
                            focusedBorderColor = ElegantLavender, unfocusedBorderColor = ElegantDarkBorder,
                            focusedContainerColor = ElegantDarkSurfaceVariant, unfocusedContainerColor = ElegantDarkSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = wRate,
                        onValueChange = { wRate = it },
                        label = { Text("Daily Wage Rate (₹)") },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ElegantTextWhite, unfocusedTextColor = ElegantTextWhite,
                            focusedBorderColor = ElegantLavender, unfocusedBorderColor = ElegantDarkBorder,
                            focusedContainerColor = ElegantDarkSurfaceVariant, unfocusedContainerColor = ElegantDarkSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showAddWorkerDialog = false },
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = ElegantTextSecondary)
                        }
                        Button(
                            onClick = {
                                val newId = "w_" + System.currentTimeMillis()
                                val tag = "SS-PAT-${wSkill.take(2).uppercase()}-${(100..999).random()}"
                                onAddWorker(
                                    WorkerProfileEntity(
                                        id = newId,
                                        userId = "u_$newId",
                                        workerIdTag = tag,
                                        name = if (wName.isNotBlank()) wName else "New Artisan",
                                        phone = if (wPhone.isNotBlank()) wPhone else "+91 98000 11223",
                                        cooperativeId = cooperative?.id ?: "coop_patna",
                                        cooperativeName = cooperative?.name ?: "Patna Shramik Vikas Sahakari Samiti",
                                        primarySkill = wSkill,
                                        secondarySkills = "Maintenance, Diagnostic",
                                        experienceYears = wExp.toIntOrNull() ?: 3,
                                        verificationStatus = "VERIFIED",
                                        certifications = wCert,
                                        customerRating = 4.8f,
                                        ratingCount = 1,
                                        completedJobs = 0,
                                        reliabilityScore = 95,
                                        dailyWageRate = wRate.toDoubleOrNull() ?: 400.0,
                                        isAvailable = true,
                                        isBusy = false,
                                        isEmergencyReady = true,
                                        lat = 25.6110,
                                        lng = 85.1440,
                                        city = "Patna",
                                        zone = "Patna Central"
                                    )
                                )
                                showAddWorkerDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElegantMintGreen,
                                contentColor = ElegantOnMintGreen
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Register", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // View Passport Modal
    selectedWorkerForPassport?.let { worker ->
        Dialog(onDismissRequest = { selectedWorkerForPassport = null }) {
            Box(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                SkillPassportCard(worker = worker)
            }
        }
    }

    // Assign Worker Dialog
    selectedBookingForAssignment?.let { booking ->
        Dialog(onDismissRequest = { selectedBookingForAssignment = null }) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Reassign Job #${booking.bookingNumber}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ElegantTextWhite)
                    Text("Service: ${booking.serviceCategory}", fontSize = 12.sp, color = ElegantTextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Available Artisans:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = ElegantTextWhite)
                    val candidateWorkers = allWorkers.filter { it.isAvailable }
                    candidateWorkers.take(5).forEach { candidate ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ElegantDarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    onAssignWorkerToBooking(booking.id, candidate.id, candidate.name)
                                    selectedBookingForAssignment = null
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(candidate.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ElegantTextWhite)
                                    Text("${candidate.primarySkill} • ★ ${candidate.customerRating}", fontSize = 11.sp, color = ElegantTextSecondary)
                                }
                                Text("Assign", color = ElegantLavender, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { selectedBookingForAssignment = null },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantDarkSurfaceVariant,
                            contentColor = ElegantTextWhite
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }

    // Resolve Dispute Modal
    selectedDisputeForResolution?.let { dispute ->
        var responseText by remember { mutableStateOf("Cooperative conducted field inspection and mediated satisfaction agreement.") }
        Dialog(onDismissRequest = { selectedDisputeForResolution = null }) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Resolve Customer Dispute", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ElegantEmergencyRed)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Issue: ${dispute.description}", fontSize = 12.sp, color = ElegantTextSecondary)

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = responseText,
                        onValueChange = { responseText = it },
                        label = { Text("Cooperative Official Settlement") },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ElegantTextWhite, unfocusedTextColor = ElegantTextWhite,
                            focusedBorderColor = ElegantLavender, unfocusedBorderColor = ElegantDarkBorder,
                            focusedContainerColor = ElegantDarkSurfaceVariant, unfocusedContainerColor = ElegantDarkSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            onResolveDispute(dispute.id, "RESOLVED", responseText)
                            selectedDisputeForResolution = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantMintGreen,
                            contentColor = ElegantOnMintGreen
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mark Resolved & Close", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiMetricCard(
    title: String,
    primaryValue: String,
    secondaryValue: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 11.sp, color = ElegantTextSecondary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = primaryValue, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = accentColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = secondaryValue, fontSize = 10.sp, color = ElegantTextMuted)
        }
    }
}
