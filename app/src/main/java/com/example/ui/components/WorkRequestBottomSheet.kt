package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppLanguage
import com.example.data.model.ServiceCategoryEntity
import com.example.data.model.UserEntity
import com.example.data.model.WorkerProfileEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkRequestBottomSheet(
    selectedCategory: ServiceCategoryEntity?,
    allCategories: List<ServiceCategoryEntity>,
    currentUser: UserEntity,
    preselectedWorker: WorkerProfileEntity? = null,
    initialProblemDescription: String = "",
    language: AppLanguage = AppLanguage.ENGLISH,
    onDismiss: () -> Unit,
    onSubmitRequest: (
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
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scrollState = rememberScrollState()

    // Service Selection State
    var activeCategoryName by remember {
        mutableStateOf(selectedCategory?.name ?: allCategories.firstOrNull()?.name ?: "Plumbing")
    }

    // Contact Details State
    var customerName by remember {
        mutableStateOf(currentUser.name.takeIf { it.isNotBlank() && it != "Citizen" && it != "Guest Citizen" } ?: "")
    }
    var customerPhone by remember {
        mutableStateOf(currentUser.phone.takeIf { it.isNotBlank() && it != "N/A" } ?: "")
    }
    var serviceAddress by remember {
        mutableStateOf(
            listOfNotNull(currentUser.village.takeIf { it.isNotBlank() }, currentUser.district.takeIf { it.isNotBlank() })
                .joinToString(", ")
                .ifBlank { "Boring Road, Near Cooperative Complex" }
        )
    }
    var serviceCity by remember {
        mutableStateOf(currentUser.district.ifBlank { currentUser.city.ifBlank { "Patna" } })
    }

    // Dates & Scheduling State
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val todayStr = remember { dateFormat.format(Date()) }
    val tomorrowStr = remember {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, 1)
        dateFormat.format(c.time)
    }
    val inTwoDaysStr = remember {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, 2)
        dateFormat.format(c.time)
    }

    var selectedDate by remember { mutableStateOf(todayStr) }
    var customDateText by remember { mutableStateOf("") }
    var isCustomDateActive by remember { mutableStateOf(false) }

    // Time Slot State
    val timeSlots = listOf(
        "Morning (9 AM - 12 PM)",
        "Afternoon (12 PM - 4 PM)",
        "Evening (4 PM - 8 PM)",
        "Immediate / Urgent"
    )
    var selectedTimeSlot by remember { mutableStateOf(timeSlots.first()) }

    // Work Specifications State
    var workDescription by remember {
        mutableStateOf(initialProblemDescription.ifBlank { "" })
    }
    var urgencyLevel by remember { mutableStateOf("NORMAL") } // NORMAL, PRIORITY, EMERGENCY

    // Step state for user requested flow:
    // "show booking confirmation or waiting on the spot and generate bill and ask for payment than book"
    var currentStep by remember { mutableIntStateOf(1) } // 1: Form, 2: Generated Bill & Payment, 3: Confirmation / Waiting on Spot
    var selectedPaymentMethod by remember { mutableStateOf("ON_THE_SPOT") } // "ON_THE_SPOT", "ONLINE_UPI"
    var isWaitingOnSpot by remember { mutableStateOf(true) }

    // Pricing calculation
    val baseRate = remember(preselectedWorker, activeCategoryName) {
        preselectedWorker?.dailyWageRate ?: 500.0
    }
    val urgencyMultiplier = when (urgencyLevel) {
        "PRIORITY" -> 1.15
        "EMERGENCY" -> 1.30
        else -> 1.0
    }
    val laborCost = (baseRate * urgencyMultiplier).toInt().toDouble()
    // "add 2% extra emergency charge"
    val isEmergency = urgencyLevel == "EMERGENCY"
    val emergencyExtraCharge = if (isEmergency) (laborCost * 0.02).toInt().toDouble().coerceAtLeast(10.0) else 0.0
    val welfareFee = 25.0
    val totalEstimatedAmount = laborCost + emergencyExtraCharge + welfareFee

    val isFormValid = customerName.trim().isNotBlank() &&
            customerPhone.trim().isNotBlank() &&
            serviceAddress.trim().isNotBlank() &&
            (if (isCustomDateActive) customDateText.trim().isNotBlank() else selectedDate.isNotBlank())

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ElegantDarkSurface,
        contentColor = ElegantTextPrimary,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = ElegantDarkBorder,
                modifier = Modifier.padding(top = 10.dp)
            )
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = ElegantLavenderContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Handyman,
                                    contentDescription = null,
                                    tint = ElegantLavender,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "कार्य अनुरोध प्रपत्र" else "Submit Work Request",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElegantTextWhite
                        )
                    }
                    Text(
                        text = if (language == AppLanguage.HINDI)
                            "प्रमाणित सहकारी कारीगरों के साथ कार्य विवरण एवं समय साझा करें"
                        else
                            "Fill details to book certified cooperative trade artisans",
                        fontSize = 12.sp,
                        color = ElegantTextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_work_request_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = ElegantTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step Progress Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    1 to "1. Details",
                    2 to "2. Bill & Pay",
                    3 to "3. Confirmation"
                ).forEach { (stepNum, stepTitle) ->
                    val isActive = currentStep == stepNum
                    val isCompleted = currentStep > stepNum
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            isActive -> ElegantLavender
                            isCompleted -> ElegantMintGreen
                            else -> ElegantDarkSurface
                        },
                        border = BorderStroke(1.dp, if (isActive || isCompleted) Color.Transparent else ElegantDarkBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = ElegantDarkBg,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = stepTitle,
                                fontSize = 11.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                color = if (isActive || isCompleted) ElegantDarkBg else ElegantTextSecondary
                            )
                        }
                    }
                }
            }

            if (currentStep == 1) {
                // 1. Service / Trade Selection
                Text(
                    text = "1. Selected Service / Trade",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ElegantLavender
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allCategories.forEach { cat ->
                    val isSelected = activeCategoryName == cat.name
                    FilterChip(
                        selected = isSelected,
                        onClick = { activeCategoryName = cat.name },
                        label = {
                            Text(
                                text = if (language == AppLanguage.HINDI) cat.hindiName else cat.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElegantLavender,
                            selectedLabelColor = ElegantOnLavender,
                            containerColor = ElegantDarkSurfaceVariant,
                            labelColor = ElegantTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = ElegantDarkBorder,
                            selectedBorderColor = ElegantLavender
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Contact Details Section
            Surface(
                color = ElegantDarkSurfaceVariant,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ElegantDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = ElegantLavender,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "2. Contact Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ElegantTextWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Customer Name
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Your Full Name") },
                        placeholder = { Text("e.g. Ramesh Verma") },
                        leadingIcon = {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = ElegantTextSecondary)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElegantLavender,
                            unfocusedBorderColor = ElegantDarkBorder,
                            focusedTextColor = ElegantTextWhite,
                            unfocusedTextColor = ElegantTextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("work_request_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Phone Number
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Contact Phone Number") },
                        placeholder = { Text("10-digit mobile number") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = ElegantTextSecondary)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElegantLavender,
                            unfocusedBorderColor = ElegantDarkBorder,
                            focusedTextColor = ElegantTextWhite,
                            unfocusedTextColor = ElegantTextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("work_request_phone_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Service Address
                    OutlinedTextField(
                        value = serviceAddress,
                        onValueChange = { serviceAddress = it },
                        label = { Text("Service Location / Street / Landmark") },
                        placeholder = { Text("e.g. House 42, Boring Road, Near SBI") },
                        leadingIcon = {
                            Icon(Icons.Default.Home, contentDescription = null, tint = ElegantTextSecondary)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElegantLavender,
                            unfocusedBorderColor = ElegantDarkBorder,
                            focusedTextColor = ElegantTextWhite,
                            unfocusedTextColor = ElegantTextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("work_request_address_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // City / District
                    OutlinedTextField(
                        value = serviceCity,
                        onValueChange = { serviceCity = it },
                        label = { Text("City / District") },
                        leadingIcon = {
                            Icon(Icons.Default.LocationCity, contentDescription = null, tint = ElegantTextSecondary)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElegantLavender,
                            unfocusedBorderColor = ElegantDarkBorder,
                            focusedTextColor = ElegantTextWhite,
                            unfocusedTextColor = ElegantTextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("work_request_city_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Preferred Date & Time Slot
            Surface(
                color = ElegantDarkSurfaceVariant,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ElegantDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = ElegantLavender,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "3. Preferred Date & Time Slot",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ElegantTextWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Select Date", fontSize = 12.sp, color = ElegantTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Quick date chip 1: Today
                        val isToday = !isCustomDateActive && selectedDate == todayStr
                        FilterChip(
                            selected = isToday,
                            onClick = {
                                isCustomDateActive = false
                                selectedDate = todayStr
                            },
                            label = { Text("Today ($todayStr)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElegantLavender,
                                selectedLabelColor = ElegantOnLavender,
                                containerColor = ElegantDarkSurface,
                                labelColor = ElegantTextSecondary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Quick date chip 2: Tomorrow
                        val isTomorrow = !isCustomDateActive && selectedDate == tomorrowStr
                        FilterChip(
                            selected = isTomorrow,
                            onClick = {
                                isCustomDateActive = false
                                selectedDate = tomorrowStr
                            },
                            label = { Text("Tomorrow ($tomorrowStr)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElegantLavender,
                                selectedLabelColor = ElegantOnLavender,
                                containerColor = ElegantDarkSurface,
                                labelColor = ElegantTextSecondary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Quick date chip 3: In 2 Days
                        val isTwoDays = !isCustomDateActive && selectedDate == inTwoDaysStr
                        FilterChip(
                            selected = isTwoDays,
                            onClick = {
                                isCustomDateActive = false
                                selectedDate = inTwoDaysStr
                            },
                            label = { Text(inTwoDaysStr, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElegantLavender,
                                selectedLabelColor = ElegantOnLavender,
                                containerColor = ElegantDarkSurface,
                                labelColor = ElegantTextSecondary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Custom Date Chip
                        FilterChip(
                            selected = isCustomDateActive,
                            onClick = { isCustomDateActive = true },
                            label = { Text("Custom Date", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElegantLavender,
                                selectedLabelColor = ElegantOnLavender,
                                containerColor = ElegantDarkSurface,
                                labelColor = ElegantTextSecondary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    if (isCustomDateActive) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customDateText,
                            onValueChange = { customDateText = it },
                            label = { Text("Enter Specific Date") },
                            placeholder = { Text("e.g. 15 Sep 2026") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Event, contentDescription = null, tint = ElegantTextSecondary)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElegantLavender,
                                unfocusedBorderColor = ElegantDarkBorder,
                                focusedTextColor = ElegantTextWhite,
                                unfocusedTextColor = ElegantTextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("work_request_date_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Preferred Time Slot", fontSize = 12.sp, color = ElegantTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        timeSlots.forEach { slot ->
                            val isSelected = selectedTimeSlot == slot
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) ElegantLavenderContainer else ElegantDarkSurface,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) ElegantLavender else ElegantDarkBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedTimeSlot = slot }
                                    .testTag("time_slot_${slot.take(7)}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = when {
                                                slot.contains("Morning") -> Icons.Default.WbSunny
                                                slot.contains("Afternoon") -> Icons.Default.LightMode
                                                slot.contains("Evening") -> Icons.Default.NightsStay
                                                else -> Icons.Default.Bolt
                                            },
                                            contentDescription = null,
                                            tint = if (isSelected) ElegantLavender else ElegantTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = slot,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) ElegantTextWhite else ElegantTextPrimary
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = ElegantLavender,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Problem Description & Urgency
            Surface(
                color = ElegantDarkSurfaceVariant,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ElegantDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = null,
                            tint = ElegantLavender,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "4. Work Description & Urgency",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ElegantTextWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = workDescription,
                        onValueChange = { workDescription = it },
                        label = { Text("Problem Description (Optional)") },
                        placeholder = { Text("Describe the issue (or tap mic to speak in Hindi/English)...") },
                        minLines = 3,
                        maxLines = 5,
                        trailingIcon = {
                            VoiceInputIconButton(
                                onTranscribedText = { txt ->
                                    workDescription = if (workDescription.isBlank()) txt else "$workDescription $txt"
                                },
                                buttonTag = "work_request_voice_btn"
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElegantLavender,
                            unfocusedBorderColor = ElegantDarkBorder,
                            focusedTextColor = ElegantTextWhite,
                            unfocusedTextColor = ElegantTextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("work_request_description_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Dispatch Urgency", fontSize = 12.sp, color = ElegantTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val urgencies = listOf(
                            Triple("NORMAL", "Standard", ElegantLavender),
                            Triple("PRIORITY", "Priority", SahakaarSaffron),
                            Triple("EMERGENCY", "Emergency", ElegantEmergencyRed)
                        )

                        urgencies.forEach { (key, label, accentColor) ->
                            val isSelected = urgencyLevel == key
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) accentColor.copy(alpha = 0.2f) else ElegantDarkSurface,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) accentColor else ElegantDarkBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { urgencyLevel = key }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) accentColor else ElegantTextSecondary
                                    )
                                }
                            }
                        }
                    }

                    if (isEmergency) {
                        Surface(
                            color = ElegantEmergencyRed.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, ElegantEmergencyRed.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = ElegantEmergencyRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🚨 Emergency Alert: Includes 2% extra emergency charge (+₹${emergencyExtraCharge.toInt()}) for instant on-the-spot dispatch.",
                                    color = ElegantEmergencyRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Assigned Artisan / Auto-Match Info Card
            Surface(
                color = ElegantDarkSurfaceVariant,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ElegantDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (preselectedWorker != null) ElegantMintGreenContainer else ElegantLavenderContainer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (preselectedWorker != null) Icons.Default.Person else Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (preselectedWorker != null) ElegantMintGreen else ElegantLavender,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        if (preselectedWorker != null) {
                            Text(
                                text = "Requested Artisan: ${preselectedWorker.name}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = ElegantTextWhite
                            )
                            Text(
                                text = "${preselectedWorker.workerIdTag} • ${preselectedWorker.cooperativeName}",
                                fontSize = 11.sp,
                                color = ElegantTextSecondary
                            )
                        } else {
                            Text(
                                text = "Cooperative Auto-Dispatch",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = ElegantTextWhite
                            )
                            Text(
                                text = "Nearest certified cooperative artisan will be matched based on trade & locality",
                                fontSize = 11.sp,
                                color = ElegantTextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Pricing Breakdown & Transparent Cooperative Welfare Quote
            Surface(
                color = ElegantDarkSurface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ElegantDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Transparent Pricing & Welfare Guarantee",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = ElegantLavender
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Base Labor Charge:", fontSize = 12.sp, color = ElegantTextSecondary)
                        Text("₹${laborCost.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ElegantTextWhite)
                    }

                    if (isEmergency) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("🚨 Emergency Surcharge (2% extra):", fontSize = 12.sp, color = ElegantEmergencyRed)
                            Text("₹${emergencyExtraCharge.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ElegantEmergencyRed)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Cooperative Welfare Fund:", fontSize = 12.sp, color = ElegantTextSecondary)
                        Text("₹${welfareFee.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ElegantTextWhite)
                    }

                    HorizontalDivider(
                        color = ElegantDarkBorder,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Estimated Total Quote:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ElegantTextWhite)
                        Text(
                            text = "₹${totalEstimatedAmount.toInt()}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = ElegantLavender
                        )
                    }
                    Text(
                        text = "Pay upon service satisfaction via UPI or cash directly to artisan.",
                        fontSize = 10.sp,
                        color = ElegantTextMuted,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step 1 Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantTextSecondary),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = { currentStep = 2 },
                    enabled = isFormValid,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElegantLavender,
                        contentColor = ElegantOnLavender,
                        disabledContainerColor = ElegantDarkSurfaceHighlight,
                        disabledContentColor = ElegantTextMuted
                    ),
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp)
                        .testTag("submit_work_request_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generate Bill & Pay →",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        } else if (currentStep == 2) {
            // -----------------------------------------------------------------
            // STEP 2: GENERATED BILL & PAYMENT SELECTION
            // -----------------------------------------------------------------
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceVariant),
                border = BorderStroke(1.dp, ElegantDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Trade: $activeCategoryName",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ElegantTextWhite
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isEmergency) ElegantEmergencyRed.copy(alpha = 0.2f) else ElegantLavenderContainer
                        ) {
                            Text(
                                text = if (isEmergency) "EMERGENCY" else urgencyLevel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isEmergency) ElegantEmergencyRed else ElegantLavender,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Artisan: ${preselectedWorker?.name ?: "Cooperative Auto-Dispatch"}",
                        fontSize = 12.sp,
                        color = ElegantTextSecondary
                    )
                    Text(
                        text = "Destination: $serviceAddress, $serviceCity",
                        fontSize = 12.sp,
                        color = ElegantTextSecondary
                    )
                    Text(
                        text = "Schedule: ${if (isCustomDateActive) customDateText else selectedDate}, $selectedTimeSlot",
                        fontSize = 12.sp,
                        color = ElegantTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Official Generated Bill Card
            Surface(
                color = ElegantDarkSurface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ElegantLavender.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = ElegantLavender,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Official Cooperative Bill",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = ElegantLavender
                            )
                        }
                        Text(
                            text = "OFFICIAL INVOICE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElegantMintGreen
                        )
                    }

                    HorizontalDivider(
                        color = ElegantDarkBorder,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Base Labor Charge:", fontSize = 12.sp, color = ElegantTextSecondary)
                        Text("₹${laborCost.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ElegantTextWhite)
                    }

                    if (isEmergency) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("🚨 Emergency Surcharge (2% extra):", fontSize = 12.sp, color = ElegantEmergencyRed)
                            Text("₹${emergencyExtraCharge.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ElegantEmergencyRed)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Cooperative Welfare & Insurance Levy:", fontSize = 12.sp, color = ElegantTextSecondary)
                        Text("₹${welfareFee.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ElegantTextWhite)
                    }

                    HorizontalDivider(
                        color = ElegantDarkBorder,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Payable Bill:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ElegantTextWhite)
                            Text("Cooperative statutory rate", fontSize = 10.sp, color = ElegantTextMuted)
                        }
                        Text(
                            text = "₹${totalEstimatedAmount.toInt()}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = ElegantMintGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Waiting on the Spot Toggle Option
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isWaitingOnSpot) ElegantMintGreen.copy(alpha = 0.12f) else ElegantDarkSurface,
                border = BorderStroke(
                    1.dp,
                    if (isWaitingOnSpot) ElegantMintGreen.copy(alpha = 0.5f) else ElegantDarkBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { isWaitingOnSpot = !isWaitingOnSpot }
                    .testTag("toggle_waiting_on_spot")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isWaitingOnSpot,
                        onCheckedChange = { isWaitingOnSpot = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = ElegantMintGreen,
                            checkmarkColor = ElegantDarkBg
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Waiting on the Spot",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isWaitingOnSpot) ElegantMintGreen else ElegantTextWhite
                        )
                        Text(
                            text = "Worker is informed you are at the spot and will dispatch directly upon booking confirmation.",
                            fontSize = 11.sp,
                            color = ElegantTextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Payment Selection Mode
            Text(
                text = "Payment Method",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = ElegantLavender
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Option 1: On the Spot / Arrival
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedPaymentMethod == "ON_THE_SPOT") ElegantLavenderContainer else ElegantDarkSurface,
                    border = BorderStroke(
                        1.dp,
                        if (selectedPaymentMethod == "ON_THE_SPOT") ElegantLavender else ElegantDarkBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedPaymentMethod = "ON_THE_SPOT" }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = if (selectedPaymentMethod == "ON_THE_SPOT") ElegantLavender else ElegantTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pay on Spot",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (selectedPaymentMethod == "ON_THE_SPOT") ElegantLavender else ElegantTextWhite
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Cash or direct UPI to worker upon arrival / completion",
                            fontSize = 10.sp,
                            color = ElegantTextSecondary,
                            lineHeight = 13.sp
                        )
                    }
                }

                // Option 2: Online Escrow
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedPaymentMethod == "ONLINE_UPI") ElegantLavenderContainer else ElegantDarkSurface,
                    border = BorderStroke(
                        1.dp,
                        if (selectedPaymentMethod == "ONLINE_UPI") ElegantLavender else ElegantDarkBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedPaymentMethod = "ONLINE_UPI" }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = if (selectedPaymentMethod == "ONLINE_UPI") ElegantLavender else ElegantTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Online UPI",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (selectedPaymentMethod == "ONLINE_UPI") ElegantLavender else ElegantTextWhite
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Instant cooperative escrow protection",
                            fontSize = 10.sp,
                            color = ElegantTextSecondary,
                            lineHeight = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step 2 Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { currentStep = 1 },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantTextSecondary),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text("← Edit", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = { currentStep = 3 },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElegantMintGreen,
                        contentColor = ElegantDarkBg
                    ),
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp)
                        .testTag("btn_proceed_to_booking")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Confirm & Book Now",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            // -----------------------------------------------------------------
            // STEP 3: BOOKING CONFIRMATION & WAITING ON THE SPOT
            // -----------------------------------------------------------------
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceVariant),
                border = BorderStroke(1.dp, ElegantMintGreen.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = ElegantMintGreen.copy(alpha = 0.15f),
                        border = BorderStroke(2.dp, ElegantMintGreen),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = ElegantMintGreen,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Booking Confirmed!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = ElegantTextWhite
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ElegantLavenderContainer,
                        border = BorderStroke(1.dp, ElegantLavender.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "STATUS: ASSIGNED",
                            color = ElegantLavender,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isWaitingOnSpot) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ElegantMintGreen.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, ElegantMintGreen.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = ElegantMintGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Waiting On The Spot: Artisan has been alerted that you are waiting on the spot at $serviceAddress.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ElegantMintGreen
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Summary Info Table
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ElegantDarkSurface,
                        border = BorderStroke(1.dp, ElegantDarkBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Assigned Artisan:", fontSize = 12.sp, color = ElegantTextSecondary)
                                Text(
                                    text = preselectedWorker?.name ?: "Patna Central Artisan",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElegantTextWhite
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Trade / Service:", fontSize = 12.sp, color = ElegantTextSecondary)
                                Text(activeCategoryName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ElegantTextWhite)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Bill:", fontSize = 12.sp, color = ElegantTextSecondary)
                                Text("₹${totalEstimatedAmount.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ElegantMintGreen)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Payment Choice:", fontSize = 12.sp, color = ElegantTextSecondary)
                                Text(
                                    if (selectedPaymentMethod == "ONLINE_UPI") "Cooperative Escrow" else "Pay on Arrival / Spot",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ElegantLavender
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step 3 Completion Button
            Button(
                onClick = {
                    val finalDate = if (isCustomDateActive) customDateText.trim() else selectedDate
                    val finalDesc = if (workDescription.trim().isNotBlank()) {
                        workDescription.trim()
                    } else {
                        "Standard service request for $activeCategoryName"
                    }
                    onSubmitRequest(
                        activeCategoryName,
                        finalDesc,
                        urgencyLevel,
                        urgencyLevel == "EMERGENCY",
                        customerName.trim(),
                        customerPhone.trim(),
                        serviceAddress.trim(),
                        serviceCity.trim(),
                        finalDate,
                        selectedTimeSlot,
                        preselectedWorker,
                        totalEstimatedAmount
                    )
                    onDismiss()
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElegantLavender,
                    contentColor = ElegantOnLavender
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(bottom = 8.dp)
                    .testTag("btn_done_booking")
            ) {
                Icon(
                    imageVector = Icons.Default.AssignmentTurnedIn,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Done • View in My Bookings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
}

