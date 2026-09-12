package com.example.ui.screens.worker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.BookingStatusChip
import com.example.ui.components.SkillPassportCard
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.*

@Composable
fun WorkerDashboardScreen(
    workerProfile: WorkerProfileEntity?,
    assignedBookings: List<BookingEntity>,
    availableWork: List<BookingEntity> = emptyList(),
    welfareWallet: WelfareWalletEntity?,
    welfareTransactions: List<WelfareTransactionEntity>,
    language: AppLanguage,
    onToggleAvailability: (Boolean) -> Unit,
    onAcceptJob: (String) -> Unit = {},
    onUpdateJobStatus: (String, String) -> Unit,
    onEditProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    initialTab: Int = 0,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) } // 0: Jobs & Dashboard, 1: Welfare Wallet, 2: Skill Passport
    androidx.compose.runtime.LaunchedEffect(initialTab) {
        selectedTab = initialTab
    }

    if (workerProfile == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SahakaarNavy)
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ElegantDarkBg)
    ) {
        // Worker Profile Top Card with Online/Offline Switch
        Card(
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(ElegantLavenderContainer)
                                .border(1.5.dp, ElegantLavender, CircleShape)
                        ) {
                            Text(
                                text = workerProfile.name.take(1),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = workerProfile.name,
                                    color = ElegantTextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                VerificationBadge(status = workerProfile.verificationStatus)
                            }
                            Text(
                                text = "${workerProfile.primarySkill} • ${workerProfile.workerIdTag}",
                                color = ElegantTextSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = workerProfile.cooperativeName,
                                color = ElegantLavender,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }

                    // Online / Offline Switch
                    Column(horizontalAlignment = Alignment.End) {
                        Switch(
                            checked = workerProfile.isAvailable,
                            onCheckedChange = { onToggleAvailability(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ElegantMintGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Slate600
                            )
                        )
                        Text(
                            text = if (workerProfile.isAvailable) "ONLINE" else "OFFLINE",
                            color = if (workerProfile.isAvailable) ElegantMintGreen else ElegantTextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Elegant Dark Metric Cards (Matching HTML reference: rounded-[28px], bg #2D2F33, border #44474E)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Today's Earnings
                    // Today's Earnings calculated from actual completed jobs
                    val completedEarnings = assignedBookings.filter { it.status == "COMPLETED" }.sumOf { it.totalAmount }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(ElegantDarkSurfaceVariant)
                            .border(1.dp, ElegantDarkBorder, RoundedCornerShape(24.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "TOTAL EARNINGS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElegantTextSecondary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "₹${completedEarnings.toInt()}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElegantTextWhite
                            )
                            Text(
                                text = "${assignedBookings.count { it.status == "COMPLETED" }} completed jobs",
                                fontSize = 10.sp,
                                color = ElegantMintGreen,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    // Safety Balance
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(ElegantDarkSurfaceVariant)
                            .border(1.dp, ElegantDarkBorder, RoundedCornerShape(24.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "SAFETY BALANCE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElegantTextSecondary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "₹${(welfareWallet?.balance ?: 0.0).toInt()}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElegantTextWhite
                            )
                            Text(
                                text = "Auto-credited",
                                fontSize = 10.sp,
                                color = ElegantLavender,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Worker Navigation Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = ElegantDarkSurface,
            contentColor = ElegantLavender,
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = ElegantLavender
                    )
                }
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        "Jobs (${assignedBookings.size})",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        color = if (selectedTab == 0) ElegantLavender else ElegantTextSecondary
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        "Welfare Wallet",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        color = if (selectedTab == 1) ElegantLavender else ElegantTextSecondary
                    )
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Text(
                        "Skill Passport",
                        fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        color = if (selectedTab == 2) ElegantLavender else ElegantTextSecondary
                    )
                }
            )
        }

        // Tab Content
        when (selectedTab) {
            0 -> {
                // Jobs List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Available Open Work Section
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Available Work in Your Area",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = ElegantTextWhite
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ElegantLavenderContainer
                            ) {
                                Text(
                                    text = "${availableWork.size} Open",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElegantLavender,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (availableWork.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = ElegantDarkSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ElegantMintGreen, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "All caught up! New requests in your trade will appear here in real-time.",
                                        fontSize = 12.sp,
                                        color = ElegantTextSecondary
                                    )
                                }
                            }
                        }
                    } else {
                        items(availableWork) { work ->
                            AvailableJobCard(
                                booking = work,
                                onAccept = { onAcceptJob(work.id) }
                            )
                        }
                    }

                    // 2. Active / Assigned Jobs Section
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "My Active Jobs",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = ElegantTextWhite
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ElegantMintGreenContainer
                            ) {
                                Text(
                                    text = "${assignedBookings.size} Active",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElegantMintGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (assignedBookings.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = ElegantDarkSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Handyman, contentDescription = null, tint = Slate600, modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("No ongoing jobs right now.", fontSize = 13.sp, color = ElegantTextSecondary)
                                    Text("Accept an available job above to start working.", fontSize = 11.sp, color = ElegantTextMuted)
                                }
                            }
                        }
                    } else {
                        items(assignedBookings) { booking ->
                            WorkerJobActionCard(
                                booking = booking,
                                onUpdateStatus = { newStatus ->
                                    onUpdateJobStatus(booking.id, newStatus)
                                }
                            )
                        }
                    }
                }
            }
            1 -> {
                // Welfare Wallet View
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Cooperative Welfare Balance", fontSize = 12.sp, color = ElegantTextSecondary)
                                        Text(
                                            text = "₹${"%,.2f".format(welfareWallet?.balance ?: 0.0)}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 26.sp,
                                            color = ElegantMintGreen
                                        )
                                    }

                                    Surface(
                                        color = ElegantMintGreenContainer,
                                        shape = RoundedCornerShape(20.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantMintGreen.copy(alpha = 0.5f))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Icon(Icons.Default.HealthAndSafety, contentDescription = "Safe", tint = ElegantMintGreen, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Active Benefits", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElegantMintGreen)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Surface(
                                    color = ElegantDarkSurfaceVariant,
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        WelfareDetailRow("Life & Accident Insurance:", welfareWallet?.insuranceCovered ?: "Covered")
                                        WelfareDetailRow("Skill Training Credits:", "${welfareWallet?.trainingCredits ?: 0} Credits")
                                        WelfareDetailRow("Emergency Fund Reserve:", "₹${(welfareWallet?.emergencyAssistanceFund ?: 0.0).toInt()}")
                                        WelfareDetailRow("Co-op Total Contribution:", "₹${(welfareWallet?.totalContributions ?: 0.0).toInt()}")
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Recent Welfare Credits",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = ElegantTextWhite
                        )
                    }

                    if (welfareTransactions.isEmpty()) {
                        item {
                            Text("10% of every completed job is credited into this wallet by the cooperative.", fontSize = 12.sp, color = ElegantTextMuted)
                        }
                    } else {
                        items(welfareTransactions) { tx ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = ElegantDarkSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(tx.description, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ElegantTextWhite)
                                        Text(tx.type, fontSize = 10.sp, color = ElegantLavender, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = "+₹${tx.amount.toInt()}",
                                        color = ElegantMintGreen,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Digital Skill Passport tab
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        SkillPassportCard(worker = workerProfile)
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onEditProfile,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElegantLavender,
                                contentColor = ElegantOnLavender
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_manage_skills_profile")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Manage Skills & Professional Profile", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onLogout,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantEmergencyRed),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantEmergencyRed),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Log Out")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign Out of Worker Account", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AvailableJobCard(
    booking: BookingEntity,
    onAccept: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantLavender.copy(alpha = 0.35f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ElegantLavenderContainer
                ) {
                    Text(
                        text = booking.serviceCategory.uppercase(),
                        color = ElegantLavender,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (booking.isEmergency) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ElegantEmergencyRed.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantEmergencyRed)
                    ) {
                        Text(
                            text = "🚨 EMERGENCY",
                            color = ElegantEmergencyRed,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = booking.problemDescription.ifBlank { "Service required: ${booking.serviceCategory}" },
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = ElegantTextWhite
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = ElegantLavender, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${booking.locationAddress}, ${booking.city}",
                    fontSize = 12.sp,
                    color = ElegantTextSecondary,
                    maxLines = 1
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = ElegantTextMuted, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Customer: ${booking.customerName} • ${booking.scheduledDate} (${booking.scheduledTime})",
                    fontSize = 11.sp,
                    color = ElegantTextMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Offered Payout", fontSize = 10.sp, color = ElegantTextMuted)
                    Text(
                        text = "₹${booking.laborCost.toInt()}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = ElegantMintGreen
                    )
                }

                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElegantMintGreen,
                        contentColor = ElegantOnMintGreen
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Accept Job", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun WorkerJobActionCard(
    booking: BookingEntity,
    onUpdateStatus: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Booking #${booking.bookingNumber}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = ElegantTextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = booking.problemDescription.ifBlank { booking.serviceCategory },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ElegantTextWhite,
                        maxLines = 1
                    )
                }

                BookingStatusChip(status = booking.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Location with dot (matching reference design)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(ElegantLavender)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${booking.locationAddress}, ${booking.city}",
                    fontSize = 12.sp,
                    color = ElegantTextSecondary,
                    maxLines = 1
                )
            }

            Text(
                text = "Customer: ${booking.customerName} (${booking.customerPhone})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = ElegantLavender,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Border top divider & Action Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = ElegantDarkBorder,
                        shape = RoundedCornerShape(0.dp)
                    )
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Estimated Pay",
                        fontSize = 10.sp,
                        color = ElegantTextMuted,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "₹${booking.laborCost.toInt()}.00",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = ElegantTextWhite
                    )
                }

                // Workflow State Transition Buttons
                when (booking.status) {
                    "ASSIGNED", "ON_THE_WAY" -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onUpdateStatus("CANCELLED") },
                                shape = RoundedCornerShape(20.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantEmergencyRed),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Cancel", fontSize = 11.sp, color = ElegantEmergencyRed)
                            }
                            Button(
                                onClick = { onUpdateStatus("ARRIVED") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElegantLavender,
                                    contentColor = ElegantOnLavender
                                ),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text("Mark Arrived", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    "ARRIVED" -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onUpdateStatus("CANCELLED") },
                                shape = RoundedCornerShape(20.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantEmergencyRed),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("Cancel", fontSize = 11.sp, color = ElegantEmergencyRed)
                            }
                            Button(
                                onClick = { onUpdateStatus("WORK_STARTED") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElegantLavender,
                                    contentColor = ElegantOnLavender
                                ),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text("Start Work", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    "WORK_STARTED" -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onUpdateStatus("CANCELLED") },
                                shape = RoundedCornerShape(20.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantEmergencyRed),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("Cancel", fontSize = 11.sp, color = ElegantEmergencyRed)
                            }
                            Button(
                                onClick = { onUpdateStatus("COMPLETED") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElegantMintGreen,
                                    contentColor = ElegantOnMintGreen
                                ),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text("Complete Work", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    "COMPLETED" -> {
                        Text(
                            text = "✓ Finished • Payout Credited",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElegantMintGreen
                        )
                    }
                    "CANCELLED" -> {
                        Text(
                            text = "✗ Booking Cancelled",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElegantEmergencyRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkerStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = ElegantTextSecondary)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ElegantTextWhite)
    }
}

@Composable
private fun WelfareDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = ElegantTextSecondary)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElegantTextWhite)
    }
}
