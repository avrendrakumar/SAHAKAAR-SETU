package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookingEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoPaymentSheet(
    booking: BookingEntity,
    onPaymentSuccess: (method: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf("UPI") }
    var isProcessing by remember { mutableStateOf(false) }
    val isHi = LanguageManager.isHindi

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            // Prominent Demo Banner as mandated
            Surface(
                color = Color(0xFFFEF3C7),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SahakaarGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = SahakaarAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "DEMO PAYMENT — NO REAL MONEY IS CHARGED",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.5.sp,
                            color = Color(0xFF92400E)
                        )
                        Text(
                            text = if (isHi) "यह एक सिमुलेशन भुगतान है। कोई वास्तविक राशि नहीं काटी जाएगी।" else "Simulated sandbox transaction for demonstration testing.",
                            fontSize = 10.5.sp,
                            color = Color(0xFF78350F)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isHi) "भुगतान विवरण" else "Payment Breakdown",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Cost Breakdown Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate50),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${booking.serviceName} (${if (isHi) "श्रम लागत" else "Labour"})", fontSize = 13.sp, color = Slate700)
                        Text("₹${booking.labourCost}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                    }
                    if (booking.materialCost > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (isHi) "सामग्री / पुर्जे" else "Material / Spares", fontSize = 13.sp, color = Slate700)
                            Text("₹${booking.materialCost}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isHi) "सहकारी कल्याण शुल्क (8%)" else "Cooperative Welfare Cess (8%)", fontSize = 12.sp, color = Slate600)
                        Text("₹${booking.platformFee}", fontSize = 12.sp, color = Slate700)
                    }
                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = Slate200)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isHi) "कुल देय राशि" else "Total Payable", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                        Text("₹${booking.totalAmount}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = SahakaarTeal)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isHi) "भुगतान माध्यम चुनें" else "Select Demo Payment Method",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Payment Methods
            val methods = listOf(
                Triple("UPI", if (isHi) "यूपीआई (Google Pay / PhonePe / BHIM)" else "UPI (Google Pay / PhonePe / BHIM)", Icons.Default.QrCode),
                Triple("CARD", if (isHi) "डेबिट / क्रेडिट कार्ड (RuPay / Visa)" else "Debit / Credit Card (RuPay / Visa)", Icons.Default.CreditCard),
                Triple("CASH", if (isHi) "सीधे नकद भुगतान (कारीगर को)" else "Direct Cash to Cooperative Worker", Icons.Default.Payments)
            )

            methods.forEach { (id, title, icon) ->
                val isSelected = selectedMethod == id
                Surface(
                    color = if (isSelected) PrimaryContainerLight else Slate50,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) SahakaarBlue else Slate300
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedMethod = id }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedMethod = id },
                            colors = RadioButtonDefaults.colors(selectedColor = SahakaarNavy)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(icon, contentDescription = null, tint = if (isSelected) SahakaarBlue else Slate700)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) OnPrimaryContainerLight else Slate800
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    isProcessing = true
                    onPaymentSuccess(selectedMethod)
                },
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = SahakaarGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHi) "₹${booking.totalAmount} का डेमो भुगतान करें" else "Authorize Demo Payment ₹${booking.totalAmount}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
