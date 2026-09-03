package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.model.PaymentStatus
import com.example.data.model.WorkerProfileEntity
import com.example.ui.components.LanguageManager
import com.example.ui.theme.*

@Composable
fun BookingTrackerScreen(
    booking: BookingEntity,
    worker: WorkerProfileEntity?,
    onAdvanceStatus: (BookingStatus) -> Unit,
    onOpenPayment: () -> Unit,
    onOpenInvoice: () -> Unit,
    onOpenRating: () -> Unit,
    onOpenDispute: () -> Unit,
    onOpenPassport: (WorkerProfileEntity) -> Unit,
    onBack: () -> Unit
) {
    val isHi = LanguageManager.isHindi
    val allStatuses = listOf(
        Pair(BookingStatus.PENDING, "Booking Placed"),
        Pair(BookingStatus.ASSIGNED, "Worker Assigned"),
        Pair(BookingStatus.ACCEPTED, "Job Accepted"),
        Pair(BookingStatus.ON_THE_WAY, "Worker On The Way"),
        Pair(BookingStatus.ARRIVED, "Worker Arrived"),
        Pair(BookingStatus.IN_PROGRESS, "Service In Progress"),
        Pair(BookingStatus.COMPLETED, "Service Completed")
    )

    val currentStatusIndex = allStatuses.indexOfFirst { it.first == booking.status }.coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header
        Surface(
            color = if (booking.isEmergency) SahakaarCrimson else SahakaarNavy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = if (booking.isEmergency) "🚨 LIVE EMERGENCY TRACKER" else if (isHi) "लाइव बुकिंग ट्रैकर" else "LIVE BOOKING TRACKER",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Booking Ref: ${booking.bookingCode}",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.5.sp
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {

            // Service & Status Summary Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(booking.serviceName, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Slate900)
                        Surface(
                            color = if (booking.status == BookingStatus.COMPLETED) SahakaarGreen.copy(alpha = 0.15f) else PrimaryContainerLight,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (isHi) booking.status.hindiName else booking.status.displayName,
                                color = if (booking.status == BookingStatus.COMPLETED) SahakaarGreen else SahakaarNavy,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(booking.problemDescription, fontSize = 12.sp, color = Slate600)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Slate500, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(booking.address, fontSize = 11.sp, color = Slate700)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Assigned Worker Card
            if (worker != null || booking.workerName != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isHi) "संबद्ध सहकारी कारीगर" else "Assigned Cooperative Craftsman",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate500
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(SahakaarGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (booking.workerName ?: "W").take(2).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(booking.workerName ?: "Worker", fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = Slate900)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Verified, contentDescription = "Verified", tint = SahakaarTeal, modifier = Modifier.size(14.dp))
                                }
                                Text(
                                    text = "${booking.cooperativeName ?: "Patna Cooperative"} • Tel: ${booking.workerPhone ?: "+91 94310 11200"}",
                                    fontSize = 11.sp,
                                    color = Slate600
                                )
                            }
                        }

                        if (worker != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { onOpenPassport(worker) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isHi) "कारीगर का डिजिटल कौशल पासपोर्ट देखें" else "View Worker's Digital Skill Passport & QR", fontSize = 11.5.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Real-Time Progress Stepper
            Text(
                text = if (isHi) "कार्य प्रगति स्थिति" else "Real-Time Job Progress",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    allStatuses.forEachIndexed { index, (status, label) ->
                        val isPassed = index <= currentStatusIndex
                        val isCurrent = index == currentStatusIndex

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        when {
                                            isCurrent -> SahakaarTeal
                                            isPassed -> SahakaarGreen
                                            else -> Slate200
                                        },
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isPassed) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else {
                                    Box(modifier = Modifier.size(8.dp).background(Slate400, CircleShape))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = label,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.5.sp,
                                    color = if (isPassed) Slate900 else Slate400
                                )
                                if (isCurrent) {
                                    Text("Active step right now", fontSize = 10.sp, color = SahakaarTeal, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        if (index < allStatuses.size - 1) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 11.dp)
                                    .width(2.dp)
                                    .height(14.dp)
                                    .background(if (index < currentStatusIndex) SahakaarGreen else Slate200)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Advance Status Demo Simulation Buttons (Allowing examiner/user to transition through all steps)
            if (booking.status != BookingStatus.COMPLETED) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate100),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "DEMO CONTROLS: Simulate Next Dispatch Step",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val nextStatus = when (booking.status) {
                            BookingStatus.PENDING -> BookingStatus.ASSIGNED
                            BookingStatus.ASSIGNED -> BookingStatus.ACCEPTED
                            BookingStatus.ACCEPTED -> BookingStatus.ON_THE_WAY
                            BookingStatus.ON_THE_WAY -> BookingStatus.ARRIVED
                            BookingStatus.ARRIVED -> BookingStatus.IN_PROGRESS
                            BookingStatus.IN_PROGRESS -> BookingStatus.COMPLETED
                            else -> null
                        }

                        if (nextStatus != null) {
                            Button(
                                onClick = { onAdvanceStatus(nextStatus) },
                                colors = ButtonDefaults.buttonColors(containerColor = SahakaarTeal),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Advance Status → ${nextStatus.displayName}")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Post-Completion Actions: Payment, Invoice, Rating, Dispute
            if (booking.status == BookingStatus.COMPLETED) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PrimaryContainerLight),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "🎉 Service Completed Successfully!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = OnPrimaryContainerLight
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Payment Status Button
                        if (booking.paymentStatus == PaymentStatus.PENDING) {
                            Button(
                                onClick = onOpenPayment,
                                colors = ButtonDefaults.buttonColors(containerColor = SahakaarGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pay ₹${booking.totalAmount} (Demo Sandbox)")
                            }
                        } else {
                            Surface(
                                color = SahakaarGreen.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SahakaarGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "PAID ₹${booking.totalAmount} via ${booking.paymentMethod}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = SahakaarGreen
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // View Digital Invoice Button
                        OutlinedButton(
                            onClick = onOpenInvoice,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isHi) "डिजिटल चालान (बिल) देखें" else "View Official Digital Invoice", fontSize = 12.5.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Rating Button
                        if (!booking.isRated) {
                            Button(
                                onClick = onOpenRating,
                                colors = ButtonDefaults.buttonColors(containerColor = SahakaarNavy),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isHi) "कारीगर को रेटिंग और समीक्षा दें" else "Rate & Review Craftsman", fontSize = 12.5.sp)
                            }
                        } else {
                            Text(
                                text = "✓ Rated ${booking.ratingGiven} ⭐ by you",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SahakaarAmber
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Raise Dispute Link
                        TextButton(
                            onClick = onOpenDispute,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ReportProblem, contentDescription = null, tint = SahakaarCrimson, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isHi) "कोई समस्या है? विवाद दर्ज करें" else "Any problem with work? Raise Cooperative Dispute",
                                color = SahakaarCrimson,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
