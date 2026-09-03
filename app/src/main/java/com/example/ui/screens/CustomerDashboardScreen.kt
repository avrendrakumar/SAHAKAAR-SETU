package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.data.model.ServiceCategoryEntity
import com.example.engine.AiClassificationResult
import com.example.ui.components.LanguageManager
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDashboardScreen(
    services: List<ServiceCategoryEntity>,
    activeBookings: List<BookingEntity>,
    aiResult: AiClassificationResult?,
    onClassifyProblem: (String) -> Unit,
    onClearAiClassification: () -> Unit,
    onSelectServiceToBook: (serviceName: String, isEmergency: Boolean) -> Unit,
    onTrackBooking: (BookingEntity) -> Unit,
    onViewAllBookings: () -> Unit,
    onOpenLiveMap: () -> Unit
) {
    var searchProblemText by remember { mutableStateOf("") }
    val isHi = LanguageManager.isHindi

    val samplePrompts = listOf(
        "Mere ghar ka pipe leak ho raha hai",
        "Ceiling fan sparking & MCB tripping",
        "Wooden wardrobe door lock jammed",
        "Split AC blower on but not cooling"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Customer Greeting
        Text(
            text = if (isHi) "शुभ प्रभात, राजेश जी!" else "Good Morning, Rajesh!",
            fontWeight = FontWeight.Black,
            fontSize = 22.sp,
            color = Slate900
        )
        Text(
            text = if (isHi) "आज आपको किस सहकारी सेवा की आवश्यकता है?" else "What skilled cooperative service do you need today?",
            fontSize = 13.5.sp,
            color = Slate600
        )

        Spacer(modifier = Modifier.height(16.dp))

        // AI Problem Assistant Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI",
                        tint = SahakaarTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHi) "एआई सेवा सहायक (समस्या लिखें या बोलें)" else "AI Service Assistant (Describe problem in any language)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = SahakaarTeal
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchProblemText,
                    onValueChange = {
                        searchProblemText = it
                        onClassifyProblem(it)
                    },
                    placeholder = {
                        Text(
                            text = if (isHi) "उदा. मेरे घर का पाइप लीक हो रहा है..." else "e.g. Mere ghar ka pipe leak ho raha hai...",
                            fontSize = 13.sp,
                            color = Slate500
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Slate500)
                    },
                    trailingIcon = {
                        if (searchProblemText.isNotEmpty()) {
                            IconButton(onClick = {
                                searchProblemText = ""
                                onClearAiClassification()
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Slate500)
                            }
                        } else {
                            IconButton(onClick = {
                                searchProblemText = "Mere ghar ka pipe leak ho raha hai"
                                onClassifyProblem(searchProblemText)
                            }) {
                                Icon(Icons.Default.Mic, contentDescription = "Voice Input", tint = SahakaarBlue)
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Quick Prompt Chips
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isHi) "त्वरित उदाहरण सुझाव:" else "Quick suggested problems:",
                    fontSize = 11.sp,
                    color = Slate500
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    samplePrompts.forEach { prompt ->
                        Surface(
                            color = Slate100,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.clickable {
                                searchProblemText = prompt
                                onClassifyProblem(prompt)
                            }
                        ) {
                            Text(
                                text = prompt,
                                fontSize = 11.sp,
                                color = Slate700,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // AI Classification Feedback Card
                if (aiResult != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (aiResult.isEmergency) Color(0xFFFEE2E2) else PrimaryContainerLight
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = null,
                                        tint = if (aiResult.isEmergency) SahakaarCrimson else SahakaarBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "AI Identified: ${aiResult.serviceName}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (aiResult.isEmergency) Color(0xFF991B1B) else OnPrimaryContainerLight
                                    )
                                }
                                Surface(
                                    color = if (aiResult.isEmergency) SahakaarCrimson else SahakaarTeal,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = aiResult.urgency,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(aiResult.explanation, fontSize = 11.5.sp, color = Slate700)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { onSelectServiceToBook(aiResult.serviceName, aiResult.isEmergency) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (aiResult.isEmergency) SahakaarCrimson else SahakaarNavy
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (aiResult.isEmergency) "🚨 Book Verified ${aiResult.serviceName} Now" else "Proceed with ${aiResult.serviceName} Booking",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Quick Actions Row: [Book Service] [🚨 Emergency 24x7] [Live Map]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Regular Booking
            Surface(
                color = SahakaarNavy,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelectServiceToBook("Plumbing", false) }
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = SahakaarGold, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(if (isHi) "सेवा बुक करें" else "Book Service", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(if (isHi) "नियमित शेड्यूलिंग" else "Scheduled", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                }
            }

            // Emergency Booking
            Surface(
                color = SahakaarCrimson,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelectServiceToBook("Plumbing", true) }
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Emergency, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(if (isHi) "🚨 आपातकालीन" else "🚨 Emergency", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    Text(if (isHi) "20 मिनट में आगमन" else "20 Min ETA", color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp)
                }
            }

            // Live Worker Map
            Surface(
                color = SahakaarTeal,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenLiveMap() }
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(if (isHi) "लाइव मैप" else "Live Map", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(if (isHi) "नजदीकी कारीगर" else "Workers Near You", color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Upcoming / Active Bookings Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isHi) "मेरी सक्रिय बुकिंग" else "My Active Bookings",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Slate900
            )
            Text(
                text = if (isHi) "सभी देखें" else "View All (${activeBookings.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = SahakaarBlue,
                modifier = Modifier.clickable { onViewAllBookings() }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (activeBookings.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                    Text(if (isHi) "वर्तमान में कोई सक्रिय बुकिंग नहीं है।" else "No active bookings right now.", color = Slate500, fontSize = 13.sp)
                }
            }
        } else {
            val topBooking = activeBookings.first()
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTrackBooking(topBooking) }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = if (topBooking.isEmergency) SahakaarCrimson else SahakaarBlue,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (topBooking.isEmergency) "🚨 EMERGENCY" else topBooking.bookingCode,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(topBooking.serviceName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
                        }

                        Surface(
                            color = when (topBooking.status) {
                                BookingStatus.COMPLETED -> SahakaarGreen.copy(alpha = 0.15f)
                                BookingStatus.IN_PROGRESS -> SahakaarAmber.copy(alpha = 0.2f)
                                BookingStatus.ON_THE_WAY -> SahakaarBlue.copy(alpha = 0.15f)
                                else -> Slate200
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (isHi) topBooking.status.hindiName else topBooking.status.displayName,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (topBooking.status) {
                                    BookingStatus.COMPLETED -> SahakaarGreen
                                    BookingStatus.IN_PROGRESS -> SahakaarAmber
                                    BookingStatus.ON_THE_WAY -> SahakaarBlue
                                    else -> Slate700
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(if (isHi) "कारीगर" else "Assigned Worker", fontSize = 10.5.sp, color = Slate500)
                            Text(topBooking.workerName ?: "Cooperative Dispatching...", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(if (isHi) "कुल राशि" else "Total Amount", fontSize = 10.5.sp, color = Slate500)
                            Text("₹${topBooking.totalAmount}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SahakaarTeal)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onTrackBooking(topBooking) },
                        colors = ButtonDefaults.buttonColors(containerColor = SahakaarNavy),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isHi) "लाइव ट्रैकिंग एवं चालान देखें" else "Track Live Status & Invoice", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Service Categories Grid
        Text(
            text = if (isHi) "सहकारी सेवा श्रेणियां" else "Cooperative Service Categories",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Slate900
        )
        Text(
            text = if (isHi) "राज्य श्रम सहकारी संघ द्वारा सत्यापित कुशल कारीगर" else "Certified craftsmen verified by Bihar Labour Cooperatives Federation",
            fontSize = 11.5.sp,
            color = Slate600
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2-Column Grid of Services
        val displayServices = if (services.isNotEmpty()) services else emptyList()
        displayServices.chunked(2).forEach { rowPair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowPair.forEach { s ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectServiceToBook(s.name, false) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(PrimaryContainerLight, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (s.name) {
                                            "Plumbing" -> Icons.Default.Plumbing
                                            "Electrical" -> Icons.Default.ElectricalServices
                                            "Carpentry" -> Icons.Default.Carpenter
                                            "Painting" -> Icons.Default.FormatPaint
                                            "Masonry" -> Icons.Default.Construction
                                            "Welding" -> Icons.Default.Hardware
                                            "AC Repair" -> Icons.Default.AcUnit
                                            "Appliance Repair" -> Icons.Default.Microwave
                                            "Cleaning" -> Icons.Default.CleaningServices
                                            "Solar Installation" -> Icons.Default.SolarPower
                                            else -> Icons.Default.Handyman
                                        },
                                        contentDescription = s.name,
                                        tint = SahakaarNavy,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text("From ₹${s.basePrice.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SahakaarTeal)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(if (isHi) s.hindiName else s.name, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Slate900)
                            Text(s.description, fontSize = 10.5.sp, color = Slate500, maxLines = 2)
                        }
                    }
                }
                if (rowPair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Why Sahakaar Setu Guarantee Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = SahakaarGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHi) "सहकार सेतु गारंटी" else "The Sahakaar Setu Cooperative Promise",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                GuaranteeItem("✓ 100% Cooperative Verified Skilled Workers with Digital Skill Passports")
                GuaranteeItem("✓ Fair Living Wages directly to workers (Zero middleman exploitation)")
                GuaranteeItem("✓ 8% welfare contribution directly funds health & accident insurance")
                GuaranteeItem("✓ Transparent billing, official digital invoice & dispute resolution")
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun GuaranteeItem(text: String) {
    Text(
        text = text,
        color = Color(0xFFE2E8F0),
        fontSize = 11.5.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}
