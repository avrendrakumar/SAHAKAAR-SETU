package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun VerificationBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val isVerified = status.equals("VERIFIED", ignoreCase = true)
    if (isVerified) {
        // ONLY blue tick visible as requested
        Icon(
            imageVector = Icons.Default.Verified,
            contentDescription = "Verified Artisan",
            tint = Color(0xFF1DA1F2), // Sky Blue Tick
            modifier = modifier.size(18.dp)
        )
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .background(Color(0xFF2C2210), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFFFB74D).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = status,
                tint = Color(0xFFFFB74D),
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "PENDING",
                color = Color(0xFFFFB74D),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun BookingStatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    val (label, bg, fg, border) = when (status) {
        "CREATED" -> Quadruple("Created", ElegantDarkSurfaceVariant, ElegantTextSecondary, ElegantDarkBorder)
        "ASSIGNED", "ON_THE_WAY" -> Quadruple("Assigned", ElegantLavenderContainer, ElegantLavender, ElegantLavender.copy(alpha = 0.4f))
        "ARRIVED" -> Quadruple("Arrived", Color(0xFF133830), Color(0xFF4DB6AC), Color(0xFF4DB6AC).copy(alpha = 0.4f))
        "WORK_STARTED" -> Quadruple("In Progress", ElegantLavenderContainer, ElegantLavender, ElegantLavender.copy(alpha = 0.4f))
        "COMPLETED" -> Quadruple("Completed", ElegantMintGreenContainer, ElegantMintGreen, ElegantMintGreen.copy(alpha = 0.4f))
        "CANCELLED" -> Quadruple("Cancelled", Color(0xFF3B1E22), ElegantEmergencyRed, ElegantEmergencyRed.copy(alpha = 0.4f))
        else -> Quadruple(status, ElegantDarkSurfaceVariant, ElegantTextSecondary, ElegantDarkBorder)
    }

    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(20.dp))
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun AvailabilityIndicator(
    isAvailable: Boolean,
    isBusy: Boolean,
    modifier: Modifier = Modifier
) {
    val (color, text) = when {
        !isAvailable -> Pair(ElegantTextMuted, "Offline")
        isBusy -> Pair(Color(0xFFFFB74D), "Busy / On Duty")
        else -> Pair(ElegantMintGreen, "Available Now")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

