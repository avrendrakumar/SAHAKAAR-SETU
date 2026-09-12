package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookingEntity
import com.example.data.model.CooperativeEntity
import com.example.data.model.ServiceCategoryEntity
import com.example.data.model.WorkerProfileEntity
import com.example.ui.components.SkillPassportCard
import com.example.ui.theme.*

@Composable
fun LandingScreen(
    categories: List<ServiceCategoryEntity>,
    cooperatives: List<CooperativeEntity>,
    allWorkers: List<WorkerProfileEntity> = emptyList(),
    allBookings: List<BookingEntity> = emptyList(),
    featuredWorker: WorkerProfileEntity?,
    language: AppLanguage,
    isLoggedIn: Boolean = false,
    onOpenLoginPortal: () -> Unit = {},
    onBookServiceClick: () -> Unit,
    onJoinWorkerClick: () -> Unit,
    onInstitutionalClick: () -> Unit,
    onEmergencyClick: () -> Unit,
    onCategorySelected: (ServiceCategoryEntity) -> Unit,
    onBookSpecificWorker: ((WorkerProfileEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedTradeFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredWorkers = remember(selectedTradeFilter, searchQuery, allWorkers) {
        allWorkers.filter { worker ->
            val skillsList = worker.secondarySkills.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val matchesTrade = selectedTradeFilter == "All" ||
                    worker.primarySkill.contains(selectedTradeFilter, ignoreCase = true) ||
                    skillsList.any { it.contains(selectedTradeFilter, ignoreCase = true) }
            val matchesSearch = searchQuery.isBlank() ||
                    worker.name.contains(searchQuery, ignoreCase = true) ||
                    worker.primarySkill.contains(searchQuery, ignoreCase = true) ||
                    skillsList.any { it.contains(searchQuery, ignoreCase = true) }
            matchesTrade && matchesSearch
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(ElegantDarkBg)
    ) {
        val maxWidth = maxWidth
        val isWide = maxWidth > 600.dp
        val horizontalMargin = if (isWide) 24.dp else 16.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ElegantDarkBg),
            contentPadding = PaddingValues(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Guest Login Banner (if not logged in)
            if (!isLoggedIn) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 680.dp)
                            .padding(horizontal = horizontalMargin, vertical = 8.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = ElegantLavender.copy(alpha = 0.12f)),
                            border = BorderStroke(1.dp, ElegantLavender.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenLoginPortal() }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(ElegantLavender)
                                    ) {
                                        Icon(Icons.Default.LockPerson, contentDescription = null, tint = ElegantOnLavender, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Sign In to Sahakaar Setu",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = ElegantTextWhite
                                        )
                                        Text(
                                            text = "Book verified artisans, track work & access cooperative benefits",
                                            fontSize = 11.sp,
                                            color = ElegantTextSecondary
                                        )
                                    }
                                }
                                Button(
                                    onClick = onOpenLoginPortal,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ElegantLavender,
                                        contentColor = ElegantOnLavender
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text("Sign In", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Hero Section (Mirroring sahakaarsetu.vercel.app)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(ElegantDarkSurface, ElegantDarkBg)
                            )
                        )
                        .padding(horizontal = horizontalMargin, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Government & Cooperative Badge
                    Surface(
                        color = ElegantLavender.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, ElegantLavender.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "🏛️ BIHAR LABOUR COOPERATIVE DIRECTORY",
                            color = ElegantLavender,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (language == AppLanguage.HINDI) "प्रत्यक्ष श्रम सहकारिता नेटवर्क" else "Direct Labour Cooperative Network",
                        color = ElegantTextWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 30.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (language == AppLanguage.HINDI)
                            "शून्य बिचौलिया कमीशन • 100% प्रत्यक्ष कारीगर भुगतान • प्रमाणित व पृष्ठभूमि सत्यापित"
                        else
                            "Zero Middleman Commission • 100% Fair Artisan Payout • Certified & Background Verified",
                        color = ElegantLavender,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Book registered electricians, plumbers, carpenters, masons, and technicians across Bihar with government-monitored fair standard wages.",
                        color = ElegantTextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Hero Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onBookServiceClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElegantLavender,
                                contentColor = ElegantOnLavender
                            ),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.Handyman, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (language == AppLanguage.HINDI) "कारीगर बुक करें" else "Book Skilled Artisan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        OutlinedButton(
                            onClick = onJoinWorkerClick,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantTextWhite),
                            border = BorderStroke(1.dp, ElegantDarkBorder),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.Engineering, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (language == AppLanguage.HINDI) "कारीगर पंजीकरण" else "Worker Registration",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Institutional Client Button
                    OutlinedButton(
                        onClick = onInstitutionalClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantLavender),
                        border = BorderStroke(1.dp, ElegantLavender.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Business, contentDescription = null, tint = ElegantLavender, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "सरकारी व कॉर्पोरेट थोक बुकिंग (B2B)" else "B2B / Institutional Bulk Labour Booking",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Emergency Callout Banner
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                        .padding(horizontal = horizontalMargin, vertical = 6.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3B1E22)),
                        border = BorderStroke(1.dp, ElegantEmergencyRed.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEmergencyClick() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(ElegantEmergencyRed, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WarningAmber,
                                        contentDescription = "Emergency",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "🚨 24x7 Emergency Rapid Dispatch",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ElegantEmergencyRed,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Electrical sparks, pipe bursts, urgent repairs. Dispatched under 15 mins.",
                                        color = ElegantTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Go",
                                tint = ElegantEmergencyRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Real Transparency Metrics (Mirroring sahakaarsetu.vercel.app)
            item {
                val totalDisbursed = allBookings.sumOf { it.totalAmount }
                val verifiedCount = allWorkers.count { it.verificationStatus == "VERIFIED" }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                        .padding(horizontal = horizontalMargin, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricCard(
                            headline = "0%",
                            label = "Commission Cut",
                            desc = "100% to worker",
                            accentColor = ElegantMintGreen,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            headline = "${allWorkers.size}+",
                            label = "Verified Artisans",
                            desc = "$verifiedCount with Skill Passport",
                            accentColor = ElegantLavender,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            headline = "₹1,000",
                            label = "Starter Pool",
                            desc = "Worker welfare fund",
                            accentColor = ElegantLavender,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Interactive Trade Filter Pills (Mirroring sahakaarsetu.vercel.app)
            item {
                val trades = listOf(
                    "All", "Electrician", "Plumber", "Carpenter", "Mason",
                    "Painter", "AC Repair", "Welder", "Technician"
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                        .padding(horizontal = horizontalMargin, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter Artisans by Trade",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ElegantTextWhite
                        )
                        Text(
                            text = "${filteredWorkers.size} Available",
                            fontSize = 11.sp,
                            color = ElegantMintGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        trades.forEach { trade ->
                            val isSelected = selectedTradeFilter == trade
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) ElegantLavender else ElegantDarkSurface,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) ElegantLavender else ElegantDarkBorder
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { selectedTradeFilter = trade }
                            ) {
                                Text(
                                    text = trade,
                                    color = if (isSelected) ElegantOnLavender else ElegantTextWhite,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Live Verified Artisan Directory Cards (from sahakaarsetu.vercel.app)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                        .padding(horizontal = horizontalMargin, vertical = 4.dp)
                ) {
                    Text(
                        text = "Live Verified Artisan Directory",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ElegantTextWhite
                    )
                    Text(
                        text = "Background-verified members of state-registered labour cooperatives.",
                        fontSize = 11.sp,
                        color = ElegantTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            if (filteredWorkers.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 680.dp)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No artisans found matching \"$selectedTradeFilter\"", color = ElegantTextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                items(filteredWorkers) { worker ->
                    val coopName = cooperatives.find { it.id == worker.cooperativeId }?.name
                        ?: "Patna Shramik Vikas Sahakari Samiti"

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 680.dp)
                            .padding(horizontal = horizontalMargin, vertical = 5.dp)
                    ) {
                        LiveArtisanCard(
                            worker = worker,
                            cooperativeName = coopName,
                            language = language,
                            onBookClick = {
                                if (onBookSpecificWorker != null) {
                                    onBookSpecificWorker(worker)
                                } else if (!isLoggedIn) {
                                    onOpenLoginPortal()
                                } else {
                                    onBookServiceClick()
                                }
                            }
                        )
                    }
                }
            }

            // Digital Skill Passport Showcase
            featuredWorker?.let { worker ->
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 680.dp)
                            .padding(horizontal = horizontalMargin, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Digital Skill Passport Example",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ElegantTextWhite
                        )
                        Text(
                            text = "Every cooperative member holds an authenticated tamper-proof skill identity.",
                            fontSize = 11.sp,
                            color = ElegantTextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        SkillPassportCard(worker = worker, onBookClick = onBookServiceClick)
                    }
                }
            }

            // Why Sahakaar Setu Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                        .padding(horizontal = horizontalMargin, vertical = 12.dp)
                ) {
                    Text(
                        text = "Why Labour Cooperatives?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ElegantTextWhite
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ComparisonPoint(
                        icon = Icons.Default.Gavel,
                        title = "Worker-Owned Cooperative Governance",
                        desc = "No predatory 25-30% platform cuts. 100% of payment goes directly to workers."
                    )
                    ComparisonPoint(
                        icon = Icons.Default.Balance,
                        title = "Equitable Job Distribution",
                        desc = "Fair workload allocation algorithm prevents monopolies, spreading livelihoods evenly."
                    )
                    ComparisonPoint(
                        icon = Icons.Default.HealthAndSafety,
                        title = "Built-in Welfare & Insurance",
                        desc = "Direct contributions toward accident shield, medical coverage, and retirement security."
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// COMPONENT: LIVE ARTISAN CARD (Matches sahakaarsetu.vercel.app)
// -------------------------------------------------------------------------

@Composable
fun LiveArtisanCard(
    worker: WorkerProfileEntity,
    cooperativeName: String,
    language: AppLanguage,
    onBookClick: () -> Unit
) {
    val skillsList = remember(worker.secondarySkills) {
        worker.secondarySkills.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
        border = BorderStroke(1.dp, ElegantDarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Avatar, Name, Trade, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2C2E33))
                            .border(1.dp, ElegantLavender.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = when (worker.primarySkill) {
                                "Electrician" -> Icons.Default.Bolt
                                "Plumber" -> Icons.Default.Plumbing
                                "Carpenter" -> Icons.Default.Carpenter
                                "Mason" -> Icons.Default.Foundation
                                "Painter" -> Icons.Default.FormatPaint
                                "Welder" -> Icons.Default.Hardware
                                else -> Icons.Default.Engineering
                            },
                            contentDescription = worker.primarySkill,
                            tint = ElegantLavender,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = worker.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = ElegantTextWhite
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ElegantMintGreen.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, ElegantMintGreen.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "VERIFIED ✓",
                                    color = ElegantMintGreen,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "${worker.primarySkill} • ${worker.experienceYears} Yrs Exp",
                            fontSize = 11.sp,
                            color = ElegantTextSecondary
                        )
                    }
                }

                // Daily Rate Tag
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${worker.dailyWageRate.toInt()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ElegantMintGreen
                    )
                    Text(
                        text = "standard / day",
                        fontSize = 9.sp,
                        color = ElegantTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cooperative Society Label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = ElegantLavender,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = cooperativeName,
                    fontSize = 10.sp,
                    color = ElegantTextSecondary,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Skill Badges Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Rating pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2C2E33),
                    border = BorderStroke(1.dp, ElegantDarkBorder)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${worker.customerRating} (${worker.completedJobs} jobs)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElegantTextWhite
                        )
                    }
                }

                for (skill in skillsList.take(3)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF232529),
                        border = BorderStroke(1.dp, ElegantDarkBorder.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = skill,
                            fontSize = 10.sp,
                            color = ElegantTextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Button
            Button(
                onClick = onBookClick,
                colors = ButtonDefaults.buttonColors(containerColor = ElegantLavender),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = ElegantOnLavender, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Book Artisan (Fair Standard Rate)",
                    color = ElegantOnLavender,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    headline: String,
    label: String,
    desc: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
        border = BorderStroke(1.dp, ElegantDarkBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = headline, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = accentColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = label, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = ElegantTextWhite)
            Text(text = desc, fontSize = 9.sp, color = ElegantTextSecondary, lineHeight = 12.sp)
        }
    }
}

@Composable
private fun ComparisonPoint(icon: ImageVector, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(ElegantLavender.copy(alpha = 0.15f))
        ) {
            Icon(icon, contentDescription = null, tint = ElegantLavender, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ElegantTextWhite)
            Text(text = desc, fontSize = 11.sp, color = ElegantTextSecondary, lineHeight = 15.sp)
        }
    }
}
