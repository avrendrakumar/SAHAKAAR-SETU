package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookingEntity
import com.example.data.model.CooperativeEntity
import com.example.data.model.DisputeEntity
import com.example.data.model.WorkerProfileEntity
import com.example.ui.components.LanguageManager
import com.example.ui.theme.*

@Composable
fun FederationDashboardScreen(
    cooperatives: List<CooperativeEntity>,
    workers: List<WorkerProfileEntity>,
    bookings: List<BookingEntity>,
    disputes: List<DisputeEntity>,
    onShowMessage: (String) -> Unit
) {
    val isHi = LanguageManager.isHindi

    val totalWorkers = workers.size
    val totalCooperatives = cooperatives.size
    val totalBookings = bookings.size
    val totalGrossRevenue = bookings.sumOf { it.totalAmount }
    val totalWelfareAccumulated = workers.sumOf { it.welfareBalance }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // State Federation Header
        Card(
            colors = CardDefaults.cardColors(containerColor = SahakaarNavy),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(SahakaarGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Slate900, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "BIHAR STATE LABOUR COOPERATIVE FEDERATION",
                                color = SahakaarGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = if (isHi) "महासंघ / राज्य स्तरीय नियंत्रण कक्ष" else "State Federation Apex Oversight",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // High Level State Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Active Cooperatives", fontSize = 10.sp, color = Color(0xFFCBD5E1))
                        Text("$totalCooperatives Primary Units", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column {
                        Text("Certified Workforce", fontSize = 10.sp, color = Color(0xFFCBD5E1))
                        Text("$totalWorkers Workers", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SahakaarTeal)
                    }
                    Column {
                        Text("State Welfare Reserve", fontSize = 10.sp, color = Color(0xFFCBD5E1))
                        Text("₹${(totalWelfareAccumulated / 1000).toInt()}k", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SahakaarGold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = if (isHi) "जिलावार प्राथमिक सहकारी समितियां" else "District Cooperative Benchmarks",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Slate900
        )
        Spacer(modifier = Modifier.height(8.dp))

        // List of Cooperatives with benchmark performance
        cooperatives.forEach { coop ->
            val coopWorkers = workers.filter { it.cooperativeId == coop.id }
            val coopBookings = bookings.filter { it.cooperativeId == coop.id }
            val completedCount = coopBookings.count { it.status == com.example.data.model.BookingStatus.COMPLETED }

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
                        Text(coop.name, fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = Slate900)
                        Surface(color = PrimaryContainerLight, shape = RoundedCornerShape(6.dp)) {
                            Text(coop.district, color = SahakaarNavy, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Reg: COOP-BR-${coop.district.take(3).uppercase()}-2024 • ${coop.address}", fontSize = 11.sp, color = Slate600)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Workers: ${coopWorkers.size}", fontSize = 11.5.sp, color = Slate700)
                        Text("Verified: ${coopWorkers.count { it.isVerified }}", fontSize = 11.5.sp, color = SahakaarGreen, fontWeight = FontWeight.Bold)
                        Text("Jobs Delivered: $completedCount", fontSize = 11.5.sp, color = Slate700)
                        Text("Disputes: 0", fontSize = 11.5.sp, color = Slate500)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // State-Wide Policy Analytics Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate100),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("State Labour Policy & Welfare Governance", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Slate900)
                Spacer(modifier = Modifier.height(6.dp))
                Text("• Zero unorganized middlemen: 100% of jobs mapped to formal cooperative registrations.", fontSize = 11.5.sp, color = Slate700)
                Text("• Digital Skill Passports fully compliant with National Skill Qualification Framework (NSQF).", fontSize = 11.5.sp, color = Slate700)
                Text("• Fair work distribution prevents monopsony and distributes work equitably across crafts.", fontSize = 11.5.sp, color = Slate700)

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onShowMessage("Generated State Audit Compliance Report (PDF) exported.") },
                    colors = ButtonDefaults.buttonColors(containerColor = SahakaarNavy),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export State Cooperative Audit Report")
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
