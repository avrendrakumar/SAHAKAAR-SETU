package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BookingEntity
import com.example.ui.theme.*

@Composable
fun DigitalInvoiceDialog(
    booking: BookingEntity,
    onDismiss: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val isHi = LanguageManager.isHindi
    val invoiceId = remember(booking.id) { "INV-SS-2026-${String.format("%05d", booking.id * 107 + 1042)}" }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Official Invoice Top Header
                Surface(
                    color = SahakaarNavy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = SahakaarGold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isHi) "डिजिटल सेवा चालान" else "DIGITAL SERVICE INVOICE",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Text(
                                text = "Sahakaar Setu Labour Cooperative Federation",
                                color = Color(0xFFCBD5E1),
                                fontSize = 10.5.sp
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                // Invoice Body
                Column(modifier = Modifier.padding(16.dp)) {
                    // Invoice ID and Status Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "INVOICE #", fontSize = 10.sp, color = Slate500, fontWeight = FontWeight.Bold)
                            Text(text = invoiceId, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            Text(text = "Date: 02 Sep 2026", fontSize = 11.sp, color = Slate600)
                        }

                        Surface(
                            color = SahakaarGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SahakaarGreen)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SahakaarGreen, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = booking.paymentStatus.displayName.uppercase(),
                                    color = SahakaarGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = Slate200)
                    Spacer(modifier = Modifier.height(14.dp))

                    // 2-Column Party Information: Customer & Cooperative Worker
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (isHi) "बिल प्राप्तकर्ता (ग्राहक)" else "Billed To (Customer):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate500)
                            Text(booking.customerName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate900)
                            Text(booking.customerPhone, fontSize = 11.5.sp, color = Slate700)
                            Text(booking.address, fontSize = 11.sp, color = Slate600)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (isHi) "प्रदाता (सहकारी कारीगर)" else "Service Provider (Cooperative):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate500)
                            Text(booking.workerName ?: "Verified Skilled Worker", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate900)
                            Text(booking.cooperativeName ?: "Patna Shramik Sahakari", fontSize = 11.sp, color = SahakaarBlue)
                            Text("Job Code: ${booking.bookingCode}", fontSize = 10.5.sp, color = Slate600)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Service Table
                    Surface(
                        color = Slate50,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(if (isHi) "सेवा विवरण" else "Service Description", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate600)
                                Text(if (isHi) "राशि (₹)" else "Amount (₹)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate600)
                            }
                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Slate200)

                            InvoiceLine(
                                title = "${booking.serviceName} Professional Labour",
                                sub = booking.problemDescription,
                                amount = "₹${booking.labourCost}"
                            )

                            if (booking.materialCost > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                InvoiceLine(
                                    title = "Standard Materials & Replacement Parts",
                                    sub = "Cooperative verified parts with warranty",
                                    amount = "₹${booking.materialCost}"
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            InvoiceLine(
                                title = "Cooperative Welfare Cess & Platform Admin (8%)",
                                sub = "Directly allocated to worker health insurance & accident cover",
                                amount = "₹${booking.platformFee}"
                            )

                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Slate300)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(if (isHi) "कुल राशि" else "Grand Total", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Slate900)
                                Text("₹${booking.totalAmount}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = SahakaarNavy)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Transaction Metadata Box
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PrimaryContainerLight.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Payment Method:", fontSize = 11.sp, color = Slate700)
                                Text(booking.paymentMethod.ifBlank { "Demo UPI / Digital" }, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Transaction Ref:", fontSize = 11.sp, color = Slate700)
                                Text(booking.paymentTransactionId.ifBlank { "TXN-SS-889104" }, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Cooperative GSTIN / Reg:", fontSize = 11.sp, color = Slate700)
                                Text("10AAACS8892K1ZP", fontSize = 11.sp, color = Slate900)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Download, Print & Share Action Buttons (as mandated in requirement 18)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onShowMessage("Invoice PDF downloaded to device storage.") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isHi) "डाउनलोड" else "Download", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onShowMessage("Sending invoice to thermal cooperative printer...") },
                            colors = ButtonDefaults.buttonColors(containerColor = SahakaarNavy),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isHi) "प्रिंट करें" else "Print", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceLine(title: String, sub: String, amount: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
            Text(sub, fontSize = 10.5.sp, color = Slate500, maxLines = 1)
        }
        Text(amount, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
    }
}
