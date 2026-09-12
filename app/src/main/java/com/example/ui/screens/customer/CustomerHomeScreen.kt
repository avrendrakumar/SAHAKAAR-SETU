package com.example.ui.screens.customer

import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.data.ai.AiServiceClassifier
import com.example.data.ai.ClassificationResult
import com.example.data.model.*
import com.example.domain.matching.IntelligentWorkerMatcher
import com.example.domain.matching.MatchBreakdown
import com.example.ui.components.BookingStatusChip
import com.example.ui.components.SkillPassportCard
import com.example.ui.components.VerificationBadge
import com.example.ui.components.VoiceInputIconButton
import com.example.ui.components.WorkRequestBottomSheet
import com.example.ui.theme.*
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    currentUser: UserEntity,
    categories: List<ServiceCategoryEntity>,
    allWorkers: List<WorkerProfileEntity>,
    customerBookings: List<BookingEntity>,
    invoices: List<InvoiceEntity>,
    language: AppLanguage,
    onBookWorker: (WorkerProfileEntity, String, String, String, Boolean, String, Double) -> Unit,
    onMakeDemoPayment: (String, String) -> Unit,
    onSubmitRating: (String, String, Int, String) -> Unit,
    onRaiseDispute: (String, String, String, String) -> Unit,
    initialTab: Int = 0,
    onOpenAiChat: (() -> Unit)? = null,
    onSubmitWorkRequest: ((
        category: String,
        description: String,
        urgency: String,
        isEmergency: Boolean,
        customerName: String,
        customerPhone: String,
        address: String,
        city: String,
        scheduledDate: String,
        scheduledTime: String,
        worker: WorkerProfileEntity?,
        estimatedCost: Double
    ) -> Unit)? = null,
    onCancelBooking: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    androidx.compose.runtime.LaunchedEffect(initialTab) {
        selectedTab = initialTab
    }

    // Work Request Bottom Sheet State
    var showWorkRequestSheet by remember { mutableStateOf(false) }
    var selectedCategoryForSheet by remember { mutableStateOf<ServiceCategoryEntity?>(null) }
    var selectedWorkerForSheet by remember { mutableStateOf<WorkerProfileEntity?>(null) }
    var sheetProblemDescription by remember { mutableStateOf("") }

    // AI Problem State
    var problemInput by remember { mutableStateOf("") }
    var isClassifying by remember { mutableStateOf(false) }
    var classificationResult by remember { mutableStateOf<ClassificationResult?>(null) }
    var isVoiceSimulated by remember { mutableStateOf(false) }

    // Emergency Toggle
    var isEmergency by remember { mutableStateOf(false) }

    // Selected Trade
    var selectedCategory by remember { mutableStateOf("Plumbing") }

    // Customer Location
    var selectedLocation by remember { mutableStateOf("Boring Road, Patna") }

    // Matching Result
    val targetLat = remember(selectedLocation) {
        if (selectedLocation.contains("Muzaffarpur")) 26.1209
        else if (selectedLocation.contains("Gaya")) 24.7914
        else 25.6110 // Patna
    }
    val targetLng = remember(selectedLocation) {
        if (selectedLocation.contains("Muzaffarpur")) 85.3647
        else if (selectedLocation.contains("Gaya")) 85.0002
        else 85.1440 // Patna
    }

    val matchedWorkers = remember(selectedCategory, isEmergency, allWorkers, customerBookings, targetLat, targetLng) {
        IntelligentWorkerMatcher.matchWorkers(
            requiredServiceCategory = selectedCategory,
            targetLat = targetLat,
            targetLng = targetLng,
            isEmergency = isEmergency,
            workers = allWorkers,
            activeBookings = customerBookings
        )
    }

    // Dialog States
    var selectedWorkerForBooking by remember { mutableStateOf<MatchBreakdown?>(null) }
    var selectedBookingForPayment by remember { mutableStateOf<BookingEntity?>(null) }
    var selectedInvoiceToView by remember { mutableStateOf<InvoiceEntity?>(null) }
    var selectedBookingForRating by remember { mutableStateOf<BookingEntity?>(null) }
    var selectedBookingForDispute by remember { mutableStateOf<BookingEntity?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ElegantDarkBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Customer Header Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Welcome, ${currentUser.name}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = ElegantTextWhite
                            )
                            Text(
                                text = "City: ${currentUser.city} • Customer Account",
                                fontSize = 12.sp,
                                color = ElegantTextSecondary
                            )
                        }

                        // Emergency Switch
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isEmergency) ElegantEmergencyRed.copy(alpha = 0.2f) else ElegantDarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isEmergency) ElegantEmergencyRed else ElegantDarkBorder
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { isEmergency = !isEmergency }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isEmergency) "🚨 EMERGENCY ACTIVE" else "Emergency Mode",
                                    color = if (isEmergency) ElegantEmergencyRed else ElegantTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Navigation Tabs for Work Provider
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = ElegantDarkSurface,
                contentColor = ElegantLavender,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = ElegantLavender,
                        height = 3.dp
                    )
                },
                divider = {
                    HorizontalDivider(color = ElegantDarkBorder, thickness = 1.dp)
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Available Workers (${matchedWorkers.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (selectedTab == 0) ElegantLavender else ElegantTextSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "My Bookings (${customerBookings.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (selectedTab == 1) ElegantLavender else ElegantTextSecondary
                        )
                    }
                )
            }
        }

        if (selectedTab == 0) {
            // AI Problem Classification Box
            item {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = ElegantLavender,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = AppStrings.get("ai_classifier", language),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ElegantLavender
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = problemInput,
                        onValueChange = { problemInput = it },
                        placeholder = {
                            Text(
                                AppStrings.get("describe_problem", language),
                                fontSize = 12.sp,
                                color = ElegantTextMuted
                            )
                        },
                        trailingIcon = {
                            VoiceInputIconButton(
                                onTranscribedText = { transcribed ->
                                    problemInput = transcribed
                                    coroutineScope.launch {
                                        isClassifying = true
                                        val res = AiServiceClassifier.classifyProblem(transcribed)
                                        classificationResult = res
                                        selectedCategory = res.serviceCategory
                                        if (res.urgency == "EMERGENCY") isEmergency = true
                                        isClassifying = false
                                    }
                                },
                                buttonTag = "home_voice_transcribe_btn"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_problem_input_field"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ElegantTextWhite,
                            unfocusedTextColor = ElegantTextWhite,
                            focusedContainerColor = ElegantDarkSurface,
                            unfocusedContainerColor = ElegantDarkSurface,
                            focusedBorderColor = ElegantLavender,
                            unfocusedBorderColor = ElegantDarkBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Voice Simulation Button & Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick Voice Simulation Toggle
                        FilledTonalButton(
                            onClick = {
                                isVoiceSimulated = !isVoiceSimulated
                                if (isVoiceSimulated) {
                                    val hindiPhrases = listOf(
                                        "Mere ghar ka pipe leak ho raha hai aur pani beh raha hai.",
                                        "Ceiling fan mein spark ho raha hai aur bijli chali gayi.",
                                        "Bedroom ki deewar mein seelan aur plaster gir raha hai.",
                                        "Inverter AC cooling nahi kar raha hai, compressor band hai."
                                    )
                                    problemInput = hindiPhrases.random()
                                    coroutineScope.launch {
                                        isClassifying = true
                                        val res = AiServiceClassifier.classifyProblem(problemInput)
                                        classificationResult = res
                                        selectedCategory = res.serviceCategory
                                        if (res.urgency == "EMERGENCY") isEmergency = true
                                        isClassifying = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isVoiceSimulated) ElegantLavenderContainer else ElegantDarkSurface,
                                contentColor = if (isVoiceSimulated) ElegantLavender else ElegantTextSecondary
                            ),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice",
                                modifier = Modifier.size(16.dp),
                                tint = if (isVoiceSimulated) ElegantLavender else ElegantTextSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isVoiceSimulated) AppStrings.get("listening", language) else AppStrings.get("speak_voice", language),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isClassifying = true
                                    val res = AiServiceClassifier.classifyProblem(problemInput)
                                    classificationResult = res
                                    selectedCategory = res.serviceCategory
                                    if (res.urgency == "EMERGENCY") isEmergency = true
                                    isClassifying = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElegantLavender,
                                contentColor = ElegantOnLavender
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            if (isClassifying) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = ElegantOnLavender, strokeWidth = 2.dp)
                            } else {
                                Text("Analyze Problem", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Classification Output Badge
                    classificationResult?.let { result ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = ElegantDarkSurface,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Trade: ${result.serviceCategory} • ${result.requiredSkill}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = ElegantLavender
                                    )
                                    Surface(
                                        color = if (result.urgency == "EMERGENCY") ElegantEmergencyRed.copy(alpha = 0.2f) else ElegantDarkSurfaceVariant,
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (result.urgency == "EMERGENCY") ElegantEmergencyRed else ElegantDarkBorder)
                                    ) {
                                        Text(
                                            text = result.urgency,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (result.urgency == "EMERGENCY") ElegantEmergencyRed else ElegantLavender,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = result.explanation,
                                    fontSize = 11.sp,
                                    color = ElegantTextSecondary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                Button(
                                    onClick = {
                                        val catObj = categories.find { it.name.equals(result.serviceCategory, ignoreCase = true) }
                                            ?: categories.firstOrNull()
                                        selectedCategory = catObj?.name ?: result.serviceCategory
                                        selectedCategoryForSheet = catObj
                                        selectedWorkerForSheet = null
                                        sheetProblemDescription = problemInput.ifBlank { result.explanation }
                                        showWorkRequestSheet = true
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ElegantLavender,
                                        contentColor = ElegantOnLavender
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                        .testTag("request_ai_service_button")
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Submit Work Request for ${result.serviceCategory}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Service Category Chips & Location
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Trade",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ElegantTextWhite
                    )

                    // Location Pill
                    Surface(
                        color = ElegantDarkSurface,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                selectedLocation = when (selectedLocation) {
                                    "Boring Road, Patna" -> "Kankarbagh, Patna"
                                    "Kankarbagh, Patna" -> "Danapur, Patna"
                                    "Danapur, Patna" -> "Juran Chhapra, Muzaffarpur"
                                    "Juran Chhapra, Muzaffarpur" -> "Civil Lines, Gaya"
                                    else -> "Boring Road, Patna"
                                }
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Location", modifier = Modifier.size(12.dp), tint = ElegantLavender)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(selectedLocation, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = ElegantTextWhite)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat.name,
                            onClick = {
                                selectedCategory = cat.name
                                selectedCategoryForSheet = cat
                                selectedWorkerForSheet = null
                                sheetProblemDescription = problemInput
                                showWorkRequestSheet = true
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElegantLavender,
                                selectedLabelColor = ElegantOnLavender,
                                containerColor = ElegantDarkSurface,
                                labelColor = ElegantTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedCategory == cat.name,
                                borderColor = ElegantDarkBorder,
                                selectedBorderColor = ElegantLavender
                            ),
                            shape = RoundedCornerShape(16.dp),
                            label = {
                                Text(
                                    if (language == AppLanguage.HINDI) cat.hindiName else cat.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedCategory == cat.name) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                // Quick Action: Open Work Request Bottom Sheet for Selected Trade
                Surface(
                    color = ElegantDarkSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            val catObj = categories.find { it.name == selectedCategory } ?: categories.firstOrNull()
                            selectedCategoryForSheet = catObj
                            selectedWorkerForSheet = null
                            sheetProblemDescription = problemInput
                            showWorkRequestSheet = true
                        }
                        .testTag("open_work_request_sheet_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = ElegantLavenderContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PostAdd,
                                        contentDescription = null,
                                        tint = ElegantLavender,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Submit Work Request for $selectedCategory",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = ElegantTextWhite
                                )
                                Text(
                                    text = "Specify contact info, service address & preferred dates",
                                    fontSize = 11.sp,
                                    color = ElegantTextSecondary
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = null,
                            tint = ElegantLavender,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Recommended Workers with Exact Matching Breakdown Card
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = AppStrings.get("recommended_workers", language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ElegantTextWhite
                    )
                    Text(
                        text = "Calculated via 6-factor fair work matching engine",
                        fontSize = 11.sp,
                        color = ElegantTextSecondary
                    )
                }
                Text(
                    text = "${matchedWorkers.size} Artisans",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElegantLavender
                )
            }
        }

        if (matchedWorkers.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = ElegantDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(ElegantDarkSurfaceVariant)
                                .border(1.dp, ElegantDarkBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Engineering,
                                contentDescription = null,
                                tint = ElegantLavender,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No Verified Artisans Available",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElegantTextWhite
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "There are currently no active artisans matching '$selectedCategory' in $selectedLocation. Try choosing another trade category or clearing location filters.",
                            fontSize = 12.sp,
                            color = ElegantTextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        } else {
            items(matchedWorkers) { match ->
                WorkerMatchingCard(
                    match = match,
                    onSelectClick = {
                        val catObj = categories.find { it.name == selectedCategory } ?: categories.firstOrNull()
                        selectedCategoryForSheet = catObj
                        selectedWorkerForSheet = match.worker
                        sheetProblemDescription = problemInput
                        showWorkRequestSheet = true
                    }
                )
            }
        }
        }

        if (selectedTab == 1) {
            // Active Bookings Section
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Booked Services",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ElegantTextWhite
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ElegantMintGreenContainer
                    ) {
                        Text(
                            text = "${customerBookings.size} Requests",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElegantMintGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (customerBookings.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = ElegantDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(ElegantDarkSurfaceVariant)
                                    .border(1.dp, ElegantDarkBorder, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = ElegantLavender,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No Bookings Yet",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElegantTextWhite
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "When you request a service or hire an artisan, your active bookings, live tracking, and digital invoices will appear here.",
                                fontSize = 12.sp,
                                color = ElegantTextSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            FilledTonalButton(
                                onClick = { selectedTab = 0 },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = ElegantLavenderContainer,
                                    contentColor = ElegantLavender
                                )
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Browse Available Workers", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            } else {
                items(customerBookings) { booking ->
                    CustomerBookingCard(
                        booking = booking,
                        onPayClick = { selectedBookingForPayment = booking },
                        onViewInvoiceClick = {
                            val inv = invoices.find { it.bookingId == booking.id }
                            selectedInvoiceToView = inv
                        },
                        onRateClick = { selectedBookingForRating = booking },
                        onDisputeClick = { selectedBookingForDispute = booking },
                        onCancelClick = { onCancelBooking?.invoke(booking.id) }
                    )
                }
            }
        }
    }

    // Work Request Bottom Sheet Component
    if (showWorkRequestSheet) {
        val activeCategory = selectedCategoryForSheet
            ?: categories.find { it.name == selectedCategory }
            ?: categories.firstOrNull()
        WorkRequestBottomSheet(
            selectedCategory = activeCategory,
            allCategories = categories,
            currentUser = currentUser,
            preselectedWorker = selectedWorkerForSheet,
            initialProblemDescription = sheetProblemDescription,
            language = language,
            onDismiss = {
                showWorkRequestSheet = false
                selectedWorkerForSheet = null
            },
            onSubmitRequest = { cat, desc, urgency, isEmerg, name, phone, addr, city, date, time, worker, cost ->
                if (onSubmitWorkRequest != null) {
                    onSubmitWorkRequest(cat, desc, urgency, isEmerg, name, phone, addr, city, date, time, worker, cost)
                } else {
                    val w = worker ?: allWorkers.find { it.primarySkill.equals(cat, ignoreCase = true) } ?: allWorkers.firstOrNull()
                    if (w != null) {
                        onBookWorker(w, cat, desc, urgency, isEmerg, addr, cost)
                    }
                }
                showWorkRequestSheet = false
                selectedWorkerForSheet = null
            }
        )
    }

    // Demo Payment Modal
    selectedBookingForPayment?.let { booking ->
        var selectedMethod by remember { mutableStateOf("UPI") }
        Dialog(onDismissRequest = { selectedBookingForPayment = null }) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Service Payment Confirmation",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = ElegantTextWhite
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Booking #${booking.bookingNumber}", fontSize = 12.sp, color = ElegantTextSecondary)
                    Text(text = "Amount: ₹${booking.totalAmount.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = ElegantMintGreen)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Select Payment Method:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = ElegantTextWhite)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("UPI", "CARD", "CASH").forEach { m ->
                            FilterChip(
                                selected = selectedMethod == m,
                                onClick = { selectedMethod = m },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElegantLavender,
                                    selectedLabelColor = ElegantOnLavender,
                                    containerColor = ElegantDarkSurfaceVariant,
                                    labelColor = ElegantTextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedMethod == m,
                                    borderColor = ElegantDarkBorder,
                                    selectedBorderColor = ElegantLavender
                                ),
                                label = { Text(m, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onMakeDemoPayment(booking.id, selectedMethod)
                            selectedBookingForPayment = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantMintGreen,
                            contentColor = ElegantOnMintGreen
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Pay", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Confirm & Pay ₹${booking.totalAmount.toInt()}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // View Invoice Modal
    selectedInvoiceToView?.let { invoice ->
        Dialog(onDismissRequest = { selectedInvoiceToView = null }) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OFFICIAL INVOICE",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = ElegantLavender
                        )
                        Text(
                            text = invoice.invoiceNumber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElegantTextSecondary
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = ElegantDarkBorder)

                    PassportDetailLine("Customer:", invoice.customerName)
                    PassportDetailLine("Artisan:", invoice.workerName)
                    PassportDetailLine("Cooperative:", invoice.cooperativeName)
                    PassportDetailLine("Trade:", invoice.serviceName)
                    PassportDetailLine("Date:", invoice.issuedDate)

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = ElegantDarkSurfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            PassportDetailLine("Labor Cost:", "₹${invoice.laborCost.toInt()}")
                            PassportDetailLine("Material Cost:", "₹${invoice.materialCost.toInt()}")
                            PassportDetailLine("Cooperative Welfare Levy:", "₹${invoice.platformFee.toInt()}")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = ElegantDarkBorder)
                            PassportDetailLine("Total Paid:", "₹${invoice.totalAmount.toInt()} (${invoice.paymentStatus})")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { selectedInvoiceToView = null },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantLavender,
                            contentColor = ElegantOnLavender
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close Invoice", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Rating Dialog
    selectedBookingForRating?.let { booking ->
        var ratingVal by remember { mutableIntStateOf(5) }
        var reviewText by remember { mutableStateOf("Great cooperative service! Very punctual and clean work.") }
        Dialog(onDismissRequest = { selectedBookingForRating = null }) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Rate Artisan & Service", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = ElegantTextWhite)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Worker: ${booking.workerName}", fontSize = 13.sp, color = ElegantTextSecondary)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { ratingVal = star }) {
                                Icon(
                                    imageVector = if (star <= ratingVal) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Star $star",
                                    tint = if (star <= ratingVal) ElegantLavender else ElegantTextMuted,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        label = { Text("Review") },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ElegantTextWhite,
                            unfocusedTextColor = ElegantTextWhite,
                            focusedBorderColor = ElegantLavender,
                            unfocusedBorderColor = ElegantDarkBorder,
                            focusedContainerColor = ElegantDarkSurfaceVariant,
                            unfocusedContainerColor = ElegantDarkSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onSubmitRating(booking.id, booking.workerId, ratingVal, reviewText)
                            selectedBookingForRating = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantLavender,
                            contentColor = ElegantOnLavender
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Submit Review", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Dispute Dialog
    selectedBookingForDispute?.let { booking ->
        var disputeReason by remember { mutableStateOf("Quality of Work") }
        var disputeDesc by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { selectedBookingForDispute = null }) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Raise Dispute to Cooperative", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ElegantEmergencyRed)
                    Text("The cooperative secretary reviews all disputes within 24 hours.", fontSize = 11.sp, color = ElegantTextSecondary)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Reason:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = ElegantTextWhite)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Quality of Work", "Pricing Issue", "Worker Delay", "Behavioral").forEach { r ->
                            FilterChip(
                                selected = disputeReason == r,
                                onClick = { disputeReason = r },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElegantLavender,
                                    selectedLabelColor = ElegantOnLavender,
                                    containerColor = ElegantDarkSurfaceVariant,
                                    labelColor = ElegantTextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = disputeReason == r,
                                    borderColor = ElegantDarkBorder,
                                    selectedBorderColor = ElegantLavender
                                ),
                                label = { Text(r, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = disputeDesc,
                        onValueChange = { disputeDesc = it },
                        placeholder = { Text("Explain what happened...", fontSize = 12.sp, color = ElegantTextMuted) },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ElegantTextWhite,
                            unfocusedTextColor = ElegantTextWhite,
                            focusedBorderColor = ElegantLavender,
                            unfocusedBorderColor = ElegantDarkBorder,
                            focusedContainerColor = ElegantDarkSurfaceVariant,
                            unfocusedContainerColor = ElegantDarkSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onRaiseDispute(booking.id, booking.workerId, disputeReason, disputeDesc)
                            selectedBookingForDispute = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantEmergencyRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Submit Dispute", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun WorkerMatchingCard(
    match: MatchBreakdown,
    onSelectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Top row: Name, Avatar, Overall Match Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(ElegantDarkSurfaceVariant)
                            .border(1.dp, ElegantDarkBorder, CircleShape)
                    ) {
                        Text(
                            text = match.worker.name.take(1),
                            color = ElegantLavender,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(match.worker.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ElegantTextWhite)
                            Spacer(modifier = Modifier.width(6.dp))
                            VerificationBadge(status = match.worker.verificationStatus)
                        }
                        Text(
                            text = "${match.worker.primarySkill} • ${match.worker.cooperativeName}",
                            fontSize = 11.sp,
                            color = ElegantTextSecondary,
                            maxLines = 1
                        )
                    }
                }

                // Match Score Pill
                Surface(
                    color = if (match.overallMatchScore >= 80) ElegantMintGreenContainer else ElegantLavenderContainer,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (match.overallMatchScore >= 80) ElegantMintGreen.copy(alpha = 0.5f) else ElegantLavender.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "${match.overallMatchScore}%",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (match.overallMatchScore >= 80) ElegantMintGreen else ElegantLavender
                        )
                        Text(
                            text = "Match",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (match.overallMatchScore >= 80) ElegantMintGreen else ElegantLavender
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6-Factor Algorithm Breakdown Grid
            Surface(
                color = ElegantDarkSurfaceVariant,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        ScoreMiniPill("Skill Match (30%)", "${match.skillCompatibilityScore}%")
                        ScoreMiniPill("Distance (20%)", "${match.distanceScore}% (~${match.estimatedDistanceKm} km)")
                        ScoreMiniPill("Availability (15%)", "${match.availabilityScore}%")
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        ScoreMiniPill("Reliability (15%)", "${match.reliabilityScore}%")
                        ScoreMiniPill("Experience (10%)", "${match.worker.experienceYears} Yrs")
                        ScoreMiniPill("Fair Workload (10%)", match.workloadLabel)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "₹${match.worker.dailyWageRate.toInt()}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = ElegantTextWhite
                        )
                        Text(
                            text = " /job",
                            fontSize = 12.sp,
                            color = ElegantTextSecondary
                        )
                    }
                    Text(
                        text = "★ ${String.format("%.1f", match.worker.customerRating)} (${match.worker.ratingCount}) • ETA ~${match.estimatedEtaMinutes}m",
                        fontSize = 11.sp,
                        color = ElegantLavender,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = onSelectClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElegantLavender,
                        contentColor = ElegantOnLavender
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                    modifier = Modifier.heightIn(min = 44.dp)
                ) {
                    Text("Book Worker", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ScoreMiniPill(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 9.sp, color = ElegantTextSecondary)
        Text(text = value, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ElegantTextWhite)
    }
}

@Composable
fun CustomerBookingCard(
    booking: BookingEntity,
    onPayClick: () -> Unit,
    onViewInvoiceClick: () -> Unit,
    onRateClick: () -> Unit,
    onDisputeClick: () -> Unit,
    onCancelClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Booking identifier & Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ElegantDarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder)
                    ) {
                        Text(
                            text = "#${booking.bookingNumber}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = ElegantLavender,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = booking.serviceCategory,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElegantTextWhite
                    )
                }

                BookingStatusChip(status = booking.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary Info: Worker + Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ElegantDarkSurfaceVariant)
                            .border(1.dp, ElegantDarkBorder, CircleShape)
                    ) {
                        Text(
                            text = booking.workerName.take(1),
                            color = ElegantLavender,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = booking.workerName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ElegantTextWhite
                        )
                        Text(
                            text = "${booking.scheduledDate} • ${booking.scheduledTime}",
                            fontSize = 11.sp,
                            color = ElegantTextSecondary
                        )
                    }
                }

                // Expand / Collapse Details Button
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse details" else "Expand details",
                        tint = ElegantLavender,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Progressive Disclosure: Problem & Location Details
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(ElegantDarkSurfaceVariant, RoundedCornerShape(14.dp))
                        .border(1.dp, ElegantDarkBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    if (booking.problemDescription.isNotBlank()) {
                        Text(
                            text = "Problem Description",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElegantTextSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = booking.problemDescription,
                            fontSize = 12.sp,
                            color = ElegantTextWhite,
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                        )
                    }
                    if (booking.locationAddress.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = ElegantLavender,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${booking.locationAddress}, ${booking.city}",
                                fontSize = 11.sp,
                                color = ElegantTextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(color = ElegantDarkBorder, thickness = 0.8.dp)

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom row: Amount & Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Fee",
                        fontSize = 10.sp,
                        color = ElegantTextSecondary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "₹${booking.totalAmount.toInt()}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ElegantTextWhite
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (booking.paymentStatus == "PAID") ElegantMintGreenContainer else ElegantDarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (booking.paymentStatus == "PAID") ElegantMintGreen.copy(alpha = 0.4f) else ElegantDarkBorder
                            )
                        ) {
                            Text(
                                text = booking.paymentStatus,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (booking.paymentStatus == "PAID") ElegantMintGreen else ElegantTextSecondary,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Actions row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (booking.paymentStatus == "PENDING" && booking.status in listOf("WORK_STARTED", "COMPLETED")) {
                        Button(
                            onClick = onPayClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElegantMintGreen,
                                contentColor = ElegantOnMintGreen
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text("Pay Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (booking.paymentStatus == "PAID") {
                        OutlinedButton(
                            onClick = onViewInvoiceClick,
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantLavender.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Invoice", fontSize = 11.sp, color = ElegantLavender)
                        }

                        FilledTonalButton(
                            onClick = onRateClick,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = ElegantDarkSurfaceVariant,
                                contentColor = ElegantTextWhite
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Rate", fontSize = 11.sp)
                        }
                    }

                    if (booking.status !in listOf("COMPLETED", "CANCELLED")) {
                        onCancelClick?.let { cancelAction ->
                            OutlinedButton(
                                onClick = cancelAction,
                                shape = RoundedCornerShape(20.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantEmergencyRed.copy(alpha = 0.6f)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("Cancel", fontSize = 11.sp, color = ElegantEmergencyRed, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    IconButton(
                        onClick = onDisputeClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReportProblem,
                            contentDescription = "Raise Dispute",
                            tint = SahakaarEmergencyRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PassportDetailLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = Slate600)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate900)
    }
}
