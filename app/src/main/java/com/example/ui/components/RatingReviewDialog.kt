package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookingEntity
import com.example.ui.theme.*

@Composable
fun RatingReviewDialog(
    booking: BookingEntity,
    onSubmit: (rating: Float, review: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedStars by remember { mutableStateOf(5) }
    var reviewText by remember { mutableStateOf("") }
    val isHi = LanguageManager.isHindi

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = if (isHi) "कारीगर को रेटिंग दें" else "Rate Cooperative Worker",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    text = "${booking.workerName ?: "Worker"} • ${booking.serviceName}",
                    fontSize = 12.sp,
                    color = Slate600
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isHi) "आपका अनुभव कैसा रहा?" else "How was your service experience?",
                    fontSize = 13.sp,
                    color = Slate700
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 5-Star Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (i in 1..5) {
                        IconButton(onClick = { selectedStars = i }) {
                            Icon(
                                imageVector = if (i <= selectedStars) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = "$i Stars",
                                tint = if (i <= selectedStars) SahakaarGold else Slate300,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    label = { Text(if (isHi) "समीक्षा लिखें (वैकल्पिक)" else "Write a review (Optional)") },
                    placeholder = { Text(if (isHi) "कारीगर बहुत समयनिष्ठ और कुशल थे..." else "Worker was punctual, polite, and skilled...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 4,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedStars.toFloat(), reviewText) },
                colors = ButtonDefaults.buttonColors(containerColor = SahakaarNavy)
            ) {
                Text(if (isHi) "सबमिट करें" else "Submit Rating")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isHi) "रद्द करें" else "Cancel")
            }
        }
    )
}

@Composable
fun DisputeDialog(
    booking: BookingEntity,
    onSubmit: (reason: String, description: String) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("Work quality not satisfactory") }
    var description by remember { mutableStateOf("") }
    val isHi = LanguageManager.isHindi

    val reasons = listOf(
        "Work quality not satisfactory",
        "Problem resurfaced after departure",
        "Disagreement on materials/pricing",
        "Worker delayed or missed appointment",
        "Other issue"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isHi) "विवाद / शिकायत दर्ज करें" else "Raise a Cooperative Dispute",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = SahakaarCrimson
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Booking Ref: ${booking.bookingCode} • Worker: ${booking.workerName ?: "Assigned"}",
                    fontSize = 12.sp,
                    color = Slate600
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(if (isHi) "कारण चुनें:" else "Select Reason:", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                reasons.take(3).forEach { r ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { reason = r }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = reason == r,
                            onClick = { reason = r },
                            colors = RadioButtonDefaults.colors(selectedColor = SahakaarCrimson)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(r, fontSize = 12.sp, color = Slate800)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(if (isHi) "समस्या का विस्तार से विवरण दें" else "Describe the problem in detail") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(reason, description) },
                colors = ButtonDefaults.buttonColors(containerColor = SahakaarCrimson)
            ) {
                Text(if (isHi) "शिकायत दर्ज करें" else "Submit Dispute")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isHi) "रद्द करें" else "Cancel")
            }
        }
    )
}
