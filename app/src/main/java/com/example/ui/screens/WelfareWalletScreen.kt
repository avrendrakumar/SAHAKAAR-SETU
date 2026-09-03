package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WelfareTransactionEntity
import com.example.data.model.WorkerProfileEntity
import com.example.ui.components.LanguageManager
import com.example.ui.theme.*

@Composable
fun WelfareWalletScreen(
    worker: WorkerProfileEntity?,
    transactions: List<WelfareTransactionEntity>,
    onBack: () -> Unit
) {
    val isHi = LanguageManager.isHindi
    val currentWorker = worker ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header
        Surface(
            color = SahakaarNavy,
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
                        text = if (isHi) "श्रमिक कल्याण एवं सुरक्षा वॉलेट" else "WORKER WELFARE WALLET",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Social Security & Health Security via Labour Cooperative",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {

            // Main Wallet Balance Card (Navy-Teal Gradient)
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(SahakaarNavy, Color(0xFF0D5F7A))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isHi) "कुल संचित कल्याण कोष" else "TOTAL WELFARE RESERVE",
                                color = SahakaarGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                letterSpacing = 1.sp
                            )
                            Surface(
                                color = SahakaarGreen.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "ACTIVE POLICY",
                                    color = Color(0xFF4ADE80),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "₹${currentWorker.welfareBalance.toInt()}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp
                        )

                        Text(
                            text = if (isHi) "प्रत्येक पूर्ण कार्य से 5-8% सहकारी अंशदान" else "Accrued from each completed job via 5-8% cooperative cess",
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Welfare Support Pillars
            Text(
                text = if (isHi) "सक्रिय सामाजिक सुरक्षा लाभ" else "Active Social Security Benefits",
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(8.dp))

            val benefits = listOf(
                Triple("Ayushman Bharat / Health Cover", "₹5,00,000 cashless hospitalization for worker & family", Icons.Default.HealthAndSafety),
                Triple("Accident Insurance Coverage", "₹2,00,000 on-duty insurance through Bihar Cooperative Board", Icons.Default.Security),
                Triple("Skill Upskilling Credits", "₹3,500 available for Solar & Advanced HVAC certification", Icons.Default.School),
                Triple("Zero-Interest Emergency Advance", "Eligible for ₹5,000 instant cooperative festival/medical credit", Icons.Default.VolunteerActivism)
            )

            benefits.forEach { (title, desc, icon) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(PrimaryContainerLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = SahakaarNavy, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate900)
                            Text(desc, fontSize = 11.sp, color = Slate600)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Recent Welfare Transactions History
            Text(
                text = if (isHi) "कल्याण कोष लेन-देन विवरण" else "Welfare Fund Transaction History",
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (transactions.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("No transactions logged yet.", color = Slate500, fontSize = 12.sp)
                    }
                }
            } else {
                transactions.forEach { tx ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tx.type, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate900)
                                Text(tx.description, fontSize = 11.sp, color = Slate600, maxLines = 1)
                                Text(tx.dateString, fontSize = 10.sp, color = Slate500)
                            }
                            Text(
                                text = "${if (tx.isCredit) "+" else "-"}₹${tx.amount.toInt()}",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = if (tx.isCredit) SahakaarGreen else SahakaarCrimson
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
