package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import com.example.data.model.ServiceCategoryEntity
import com.example.data.model.WorkerProfileEntity
import com.example.engine.WorkerMatchResult
import com.example.ui.components.LanguageManager
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceBookingScreen(
    initialService: String,
    initialEmergency: Boolean,
    services: List<ServiceCategoryEntity>,
    rankedWorkers: List<WorkerMatchResult>,
    onWorkerPassportClick: (WorkerProfileEntity) -> Unit,
    onConfirmBooking: (
        serviceName: String,
        problem: String,
        address: String,
        isEmergency: Boolean,
        selectedWorker: WorkerProfileEntity?
    ) -> Unit,
    onBack: () -> Unit
) {
    var selectedService by remember { mutableStateOf(initialService) }
    var isEmergency by remember { mutableStateOf(initialEmergency) }
    var problemText by remember { mutableStateOf("") }
    var addressText by remember { mutableStateOf("Flat 302, Maurya Vihar, Boring Road, Patna") }
    var selectedWorkerMatch by remember { mutableStateOf<WorkerMatchResult?>(rankedWorkers.firstOrNull()) }
    val isHi = LanguageManager.isHindi

    val addresses = listOf(
        "Flat 302, Maurya Vihar, Boring Road, Patna",
        "House 14, IAS Colony, Bailey Road, Patna",
        "Plot 88, Kankarbagh Main Road, Patna",
        "Ashiana Nagar Phase 2, Danapur, Patna"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        Surface(
            color = if (isEmergency) SahakaarCrimson else SahakaarNavy,
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
                        text = if (isEmergency) "🚨 EMERGENCY SERVICE BOOKING" else if (isHi) "सहकारी सेवा बुकिंग" else "COOPERATIVE SERVICE BOOKING",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                    Text(
                        text = if (isEmergency) "Fast dispatch within 20 minutes" else "Verified craftsmen with Digital Skill Passports",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {

            // Service Selector Chips
            Text(
                text = if (isHi) "1. सेवा श्रेणी चुनें" else "1. Select Service Category",
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = Slate800
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                services.forEach { s ->
                    val isSelected = s.name.equals(selectedService, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedService = s.name },
                        label = { Text(if (isHi) s.hindiName else s.name, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SahakaarNavy,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Emergency Toggle Switch
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isEmergency) Color(0xFFFEE2E2) else Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isEmergency) SahakaarCrimson else Slate300
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = if (isEmergency) SahakaarCrimson else Slate600
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isHi) "24x7 तत्काल आपातकालीन सेवा" else "24x7 Immediate Emergency Dispatch",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isEmergency) Color(0xFF991B1B) else Slate900
                            )
                            Text(
                                text = if (isHi) "निकटतम उपलब्ध कारीगर 20 मिनट में पहुंचेगा" else "Guaranteed worker dispatch within 20 mins (+₹150 priority fee)",
                                fontSize = 10.5.sp,
                                color = Slate600
                            )
                        }
                    }
                    Switch(
                        checked = isEmergency,
                        onCheckedChange = { isEmergency = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = SahakaarCrimson, checkedTrackColor = Color(0xFFFCA5A5))
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Problem Description
            Text(
                text = if (isHi) "2. समस्या का विवरण" else "2. Describe the Issue",
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = Slate800
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = problemText,
                onValueChange = { problemText = it },
                placeholder = {
                    Text(
                        text = if (isHi) "उदा. वॉशबेसिन का मुख्य पाइप लीक हो रहा है..." else "e.g. Washbasin drainage pipe is broken and leaking...",
                        fontSize = 12.5.sp
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Address Selector
            Text(
                text = if (isHi) "3. सेवा का पता" else "3. Service Location in Patna",
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = Slate800
            )
            Spacer(modifier = Modifier.height(6.dp))
            addresses.forEach { addr ->
                val isSelected = addressText == addr
                Surface(
                    color = if (isSelected) PrimaryContainerLight else Color.White,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) SahakaarBlue else Slate300
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clickable { addressText = addr }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = isSelected, onClick = { addressText = addr })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(addr, fontSize = 12.sp, color = Slate800)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Intelligent Worker Matching Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isHi) "4. एआई अनुशंसित कारीगर" else "4. AI Recommended Workers",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Slate900
                    )
                    Text(
                        text = "Intelligent matching algorithm (Skill 30%, Dist 20%, Avail 15%, Rel 15%, Exp 10%, Fair Work 10%)",
                        fontSize = 9.5.sp,
                        color = Slate500
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (rankedWorkers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No workers available. Cooperative will auto-dispatch.", color = Slate500)
                }
            } else {
                rankedWorkers.take(3).forEach { match ->
                    val isSelected = selectedWorkerMatch?.worker?.id == match.worker.id
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color.White else Slate100
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            if (isSelected) SahakaarTeal else Color.Transparent
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clickable { selectedWorkerMatch = match }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(SahakaarNavy, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = match.worker.name.take(2).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(match.worker.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
                                        // Overall Match Badge
                                        Surface(
                                            color = SahakaarTeal,
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                text = "Match ${match.overallScore}%",
                                                color = Color.White,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${match.worker.primarySkill} • ${match.worker.cooperativeName}",
                                        fontSize = 11.sp,
                                        color = Slate600
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Breakdown Strip as specified in Section 10
                            Surface(
                                color = Slate50,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    MatchMetric("Skill Match", "${match.skillMatch}%")
                                    MatchMetric("Distance", "${match.distanceKm} km")
                                    MatchMetric("Reliability", "${match.reliabilityMatch}%")
                                    MatchMetric("Experience", "${match.experienceYears} yrs")
                                    MatchMetric("Workload", match.workloadLabel)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { onWorkerPassportClick(match.worker) },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = SahakaarBlue, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isHi) "डिजिटल कौशल पासपोर्ट देखें" else "View Digital Skill Passport", fontSize = 11.sp)
                                }

                                Text(
                                    text = if (isSelected) "✓ SELECTED" else "Tap to Select",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) SahakaarTeal else Slate500
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Pricing Summary Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate100),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isHi) "अनुमानित श्रम शुल्क" else "Standard Labour Charge", fontSize = 12.sp, color = Slate700)
                        Text(if (isEmergency) "₹500 (Emergency)" else "₹350", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isHi) "सहकारी कल्याण कोष (8%)" else "Cooperative Welfare Cess (8%)", fontSize = 11.sp, color = Slate600)
                        Text(if (isEmergency) "₹40" else "₹28", fontSize = 11.sp, color = Slate700)
                    }
                    Divider(modifier = Modifier.padding(vertical = 6.dp), color = Slate300)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isHi) "कुल अनुमानित राशि" else "Estimated Total", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
                        Text(if (isEmergency) "₹540" else "₹378", fontWeight = FontWeight.Black, fontSize = 16.sp, color = SahakaarTeal)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Confirm Booking Button
            Button(
                onClick = {
                    onConfirmBooking(
                        selectedService,
                        problemText.ifBlank { "Service requested via Sahakaar Setu" },
                        addressText,
                        isEmergency,
                        selectedWorkerMatch?.worker
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEmergency) SahakaarCrimson else SahakaarNavy
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEmergency) "🚨 Confirm Immediate Dispatch" else if (isHi) "सहकारी सेवा बुकिंग पक्की करें" else "Confirm Cooperative Booking",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MatchMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 9.sp, color = Slate500)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate900)
    }
}
