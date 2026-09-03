package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.model.BookingEntity
import com.example.data.model.BookingStatus
import com.example.data.model.WelfareTransactionEntity
import com.example.data.model.WorkerProfileEntity
import com.example.ui.components.LanguageManager
import com.example.ui.theme.*

@Composable
fun WorkerDashboardScreen(
    worker: WorkerProfileEntity?,
    bookings: List<BookingEntity>,
    welfareTransactions: List<WelfareTransactionEntity>,
    onToggleAvailability: (workerId: Long, current: Boolean) -> Unit,
    onUpdateBookingStatus: (bookingId: Long, status: BookingStatus) -> Unit,
    onOpenPassport: (WorkerProfileEntity) -> Unit,
    onOpenWelfareWallet: () -> Unit
) {
    val isHi = LanguageManager.isHindi
    val currentWorker = worker ?: return

    val incomingJobs = bookings.filter { it.status == BookingStatus.ASSIGNED || it.status == BookingStatus.PENDING }
    val activeJob = bookings.firstOrNull {
        it.status == BookingStatus.ACCEPTED || it.status == BookingStatus.ON_THE_WAY ||
        it.status == BookingStatus.ARRIVED || it.status == BookingStatus.IN_PROGRESS
    }
    val completedJobs = bookings.filter { it.status == BookingStatus.COMPLETED }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Worker Profile & Availability Header
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(SahakaarNavy, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentWorker.name.take(2).uppercase(),
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentWorker.name,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    color = Slate900
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Verified, contentDescription = null, tint = SahakaarTeal, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = "${currentWorker.primarySkill} • ${currentWorker.cooperativeName}",
                                fontSize = 11.5.sp,
                                color = Slate600
                            )
                        }
                    }

                    // Online/Offline Availability Switch
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Switch(
                            checked = currentWorker.isOnline,
                            onCheckedChange = { onToggleAvailability(currentWorker.id, currentWorker.isOnline) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SahakaarGreen,
                                checkedTrackColor = Color(0xFFA7F3D0)
                            )
                        )
                        Text(
                            text = if (currentWorker.isOnline) "ONLINE" else "OFFLINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentWorker.isOnline) SahakaarGreen else Slate500
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Passport and Welfare Action Buttons (Low-literacy friendly large buttons)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onOpenPassport(currentWorker) },
                        colors = ButtonDefaults.buttonColors(containerColor = SahakaarNavy),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isHi) "कौशल पासपोर्ट" else "Skill Passport", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onOpenWelfareWallet,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = SahakaarTeal, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isHi) "कल्याण वॉलेट" else "Welfare Wallet", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Worker KPI Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCard(
                title = if (isHi) "आज की कमाई" else "Today's Earnings",
                value = "₹${currentWorker.dailyEarnings.toInt()}",
                subtitle = "Direct to bank/cash",
                color = SahakaarTeal,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = if (isHi) "कल्याण कोष" else "Welfare Balance",
                value = "₹${currentWorker.welfareBalance.toInt()}",
                subtitle = "Cooperative cess",
                color = SahakaarBlue,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = if (isHi) "पूर्ण कार्य" else "Jobs Completed",
                value = "${currentWorker.completedJobs}",
                subtitle = "Rating: ${currentWorker.rating} ★",
                color = SahakaarAmber,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Active In-Progress Job Section (If any)
        if (activeJob != null) {
            Text(
                text = if (isHi) "वर्तमान चालू कार्य" else "Current Active Job",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, SahakaarTeal),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(activeJob.serviceName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                        Surface(color = SahakaarTeal, shape = RoundedCornerShape(6.dp)) {
                            Text(
                                text = activeJob.status.displayName,
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(activeJob.problemDescription, fontSize = 12.sp, color = Slate600)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Customer: ${activeJob.customerName} (${activeJob.customerPhone})", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Slate800)
                    Text("Address: ${activeJob.address}", fontSize = 11.sp, color = Slate600)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Step Advance Action Buttons (Large easy-to-tap buttons)
                    when (activeJob.status) {
                        BookingStatus.ACCEPTED -> {
                            Button(
                                onClick = { onUpdateBookingStatus(activeJob.id, BookingStatus.ON_THE_WAY) },
                                colors = ButtonDefaults.buttonColors(containerColor = SahakaarNavy),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(46.dp)
                            ) {
                                Icon(Icons.Default.DirectionsCar, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isHi) "निकल चुका हूँ (Mark On The Way)" else "Mark: On The Way", fontWeight = FontWeight.Bold)
                            }
                        }
                        BookingStatus.ON_THE_WAY -> {
                            Button(
                                onClick = { onUpdateBookingStatus(activeJob.id, BookingStatus.ARRIVED) },
                                colors = ButtonDefaults.buttonColors(containerColor = SahakaarBlue),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(46.dp)
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isHi) "पहुंच गया हूँ (Mark Arrived)" else "Mark: Arrived at Location", fontWeight = FontWeight.Bold)
                            }
                        }
                        BookingStatus.ARRIVED -> {
                            Button(
                                onClick = { onUpdateBookingStatus(activeJob.id, BookingStatus.IN_PROGRESS) },
                                colors = ButtonDefaults.buttonColors(containerColor = SahakaarAmber),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(46.dp)
                            ) {
                                Icon(Icons.Default.Build, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isHi) "काम शुरू करें (Start Work)" else "Start Service Work", fontWeight = FontWeight.Bold)
                            }
                        }
                        BookingStatus.IN_PROGRESS -> {
                            Button(
                                onClick = { onUpdateBookingStatus(activeJob.id, BookingStatus.COMPLETED) },
                                colors = ButtonDefaults.buttonColors(containerColor = SahakaarGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(46.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isHi) "काम पूरा हुआ (Complete Work & Bill)" else "Complete Work (Collect ₹${activeJob.totalAmount})", fontWeight = FontWeight.Bold)
                            }
                        }
                        else -> {}
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Incoming Job Requests (Accept / Reject)
        Text(
            text = if (isHi) "नए कार्य अनुरोध" else "New Job Requests (${incomingJobs.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Slate900
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (incomingJobs.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                    Text(if (isHi) "कोई नया कार्य अनुरोध नहीं है। आप ऑनलाइन हैं।" else "No pending requests. Keep app online for dispatches.", color = Slate500, fontSize = 12.5.sp)
                }
            }
        } else {
            incomingJobs.forEach { job ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                            Text(job.serviceName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                            Text("Payout: ₹${job.labourCost.toInt()}", fontWeight = FontWeight.Black, fontSize = 14.sp, color = SahakaarTeal)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(job.problemDescription, fontSize = 12.sp, color = Slate600)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NearMe, contentDescription = null, tint = SahakaarBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${job.address} (approx 2.1 km)", fontSize = 11.sp, color = Slate700)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Large Accept & Reject Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onUpdateBookingStatus(job.id, BookingStatus.CANCELLED) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isHi) "अस्वीकार करें" else "Decline", color = SahakaarCrimson)
                            }

                            Button(
                                onClick = { onUpdateBookingStatus(job.id, BookingStatus.ACCEPTED) },
                                colors = ButtonDefaults.buttonColors(containerColor = SahakaarGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isHi) "स्वीकार करें" else "Accept Job", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Completed Jobs History
        Text(
            text = if (isHi) "हाल ही में पूर्ण किए गए कार्य" else "Recent Completed Jobs (${completedJobs.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Slate900
        )
        Spacer(modifier = Modifier.height(8.dp))

        completedJobs.take(3).forEach { job ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate100),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(job.serviceName, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Slate900)
                        Text("Customer: ${job.customerName}", fontSize = 11.sp, color = Slate600)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("+₹${job.labourCost.toInt()}", fontWeight = FontWeight.Black, fontSize = 14.sp, color = SahakaarGreen)
                        Text(job.paymentStatus.displayName, fontSize = 10.sp, color = Slate500)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontSize = 10.sp, color = Slate600, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = color)
            Text(subtitle, fontSize = 9.sp, color = Slate500, maxLines = 1)
        }
    }
}
