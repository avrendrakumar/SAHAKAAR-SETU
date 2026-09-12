@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.data.model.WorkerProfileEntity
import com.example.ui.theme.*

/**
 * High-fidelity User Profile component allowing both Workers and Customers to manage:
 * - Professional trade details (Worker primary skill, certifications, wage rate, emergency readiness)
 * - Skills portfolio (Interactive skill chips, secondary skills addition/removal)
 * - Customer service preferences (Preferred trades, service notes)
 * - Contact information (Verified mobile, alternate phone/WhatsApp, email, full address)
 * - Security & cooperative identification
 */
@Composable
fun UserProfileComponent(
    user: UserEntity,
    workerProfile: WorkerProfileEntity? = null,
    onSaveWorkerProfile: ((
        name: String,
        email: String,
        phone: String,
        city: String,
        state: String,
        district: String,
        village: String,
        primarySkill: String,
        secondarySkills: String,
        experienceYears: Int,
        certifications: String,
        dailyWageRate: Double,
        isAvailable: Boolean,
        isEmergencyReady: Boolean,
        zone: String
    ) -> Unit)? = null,
    onSaveCustomerProfile: ((
        name: String,
        email: String,
        phone: String,
        city: String,
        state: String,
        district: String,
        village: String
    ) -> Unit)? = null,
    onChangePassword: ((oldPlain: String, newPlain: String) -> Unit)? = null,
    onLogout: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isWorker = user.role == "WORKER"

    // Edit Mode State
    var isEditing by remember { mutableStateOf(false) }
    var isChangingPassword by remember { mutableStateOf(false) }
    var activeTab by remember { mutableIntStateOf(0) } // 0: Professional & Skills, 1: Contact & Address, 2: Identity & Security

    // Common Contact Fields
    var name by remember(user) { mutableStateOf(user.name) }
    var email by remember(user) { mutableStateOf(user.email) }
    var phone by remember(user) { mutableStateOf(user.phone) }
    var alternatePhone by remember { mutableStateOf("+91 98765 43210") }
    var city by remember(user) { mutableStateOf(user.city) }
    var state by remember(user) { mutableStateOf(user.state.ifBlank { "Bihar" }) }
    var district by remember(user) { mutableStateOf(user.district.ifBlank { user.city }) }
    var village by remember(user) { mutableStateOf(user.village) }

    // Worker Professional & Skill Fields
    var primarySkill by remember(workerProfile) {
        mutableStateOf(workerProfile?.primarySkill ?: "Electrician")
    }
    var secondarySkillList by remember(workerProfile) {
        mutableStateOf(
            workerProfile?.secondarySkills?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }
                ?: listOf("Wiring", "Inverter Repair", "MCB Installation")
        )
    }
    var experienceYears by remember(workerProfile) {
        mutableIntStateOf(workerProfile?.experienceYears ?: 5)
    }
    var certifications by remember(workerProfile) {
        mutableStateOf(workerProfile?.certifications ?: "NSDC Level 4, ITI Certified Electrician")
    }
    var dailyWageRate by remember(workerProfile) {
        mutableStateOf(workerProfile?.dailyWageRate?.toInt()?.toString() ?: "650")
    }
    var isAvailable by remember(workerProfile) {
        mutableStateOf(workerProfile?.isAvailable ?: true)
    }
    var isEmergencyReady by remember(workerProfile) {
        mutableStateOf(workerProfile?.isEmergencyReady ?: true)
    }
    var zone by remember(workerProfile) {
        mutableStateOf(workerProfile?.zone ?: "Patna Central")
    }

    // Customer Specific Preferences
    var customerPreferredSkills by remember {
        mutableStateOf(listOf("Electrician", "Plumber", "Appliance Repair"))
    }
    var customerNotes by remember {
        mutableStateOf("Ring bell twice. Service preferably after 10 AM.")
    }

    // Custom skill add field
    var newCustomSkillText by remember { mutableStateOf("") }
    var showAddSkillDialog by remember { mutableStateOf(false) }

    // Password State
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }

    // Status Feedback
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    val officialId = if (isWorker) {
        workerProfile?.workerIdTag?.ifBlank { user.customIdTag } ?: "SS-WRK-000142"
    } else {
        user.customIdTag.ifBlank { "SS-CUS-000088" }
    }

    val availableTrades = listOf(
        "Electrician", "Plumber", "Carpenter", "Mason", "Painter",
        "Appliance Repair", "Welder", "Solar Tech", "AC Service", "Roofing"
    )

    val popularSkillTags = listOf(
        "Wiring", "Pipe Fitting", "Tile Work", "Wood Polishing", "Waterproofing",
        "HVAC Care", "Inverter Repair", "Leak Detection", "Switchboard Setup", "Safety Gear"
    )

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        feedbackMessage = "Copied $label to clipboard"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ElegantDarkBg)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("user_profile_component"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Top Bar with Close / Edit toggle ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onClose != null) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("btn_close_profile")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Go Back",
                                tint = ElegantTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Column(
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = if (isWorker) "Artisan Profile" else "Customer Profile",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElegantTextWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isWorker) "Manage professional details & skills" else "Manage account, skills & contact",
                            fontSize = 12.sp,
                            color = ElegantTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                FilledTonalButton(
                    onClick = {
                        if (isEditing) {
                            // Validate & Save
                            if (phone.length < 10) {
                                feedbackMessage = "Please enter a valid 10-digit mobile number"
                                return@FilledTonalButton
                            }
                            if (name.isBlank()) {
                                feedbackMessage = "Name cannot be empty"
                                return@FilledTonalButton
                            }
                            if (isWorker) {
                                val parsedWage = dailyWageRate.toDoubleOrNull() ?: 600.0
                                onSaveWorkerProfile?.invoke(
                                    name, email, phone, city, state, district, village,
                                    primarySkill, secondarySkillList.joinToString(", "),
                                    experienceYears, certifications, parsedWage,
                                    isAvailable, isEmergencyReady, zone
                                )
                            } else {
                                onSaveCustomerProfile?.invoke(
                                    name, email, phone, city, state, district, village
                                )
                            }
                            isEditing = false
                            feedbackMessage = "Profile updated successfully"
                        } else {
                            isEditing = true
                            feedbackMessage = null
                        }
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isEditing) ElegantMintGreenContainer else ElegantLavender.copy(alpha = 0.2f),
                        contentColor = if (isEditing) ElegantMintGreen else ElegantLavender
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("btn_toggle_edit_profile")
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEditing) "Save Changes" else "Edit Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        softWrap = false,
                        maxLines = 1
                    )
                }
            }
        }

        // --- Feedback message chip ---
        if (feedbackMessage != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ElegantLavender.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, ElegantLavender.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = ElegantLavender,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = feedbackMessage ?: "",
                            fontSize = 12.sp,
                            color = ElegantLavender,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // --- Official Identity Card Header ---
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                border = BorderStroke(1.dp, ElegantDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    ElegantDarkSurface,
                                    ElegantDarkSurfaceHighlight.copy(alpha = 0.7f)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Avatar Circle
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(ElegantDarkSurfaceVariant)
                                        .border(2.dp, ElegantLavender, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = if (isWorker) Icons.Default.Engineering else Icons.Default.Person,
                                        contentDescription = "Avatar",
                                        tint = ElegantLavender,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ElegantTextWhite
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = ElegantLavender.copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, ElegantLavender.copy(alpha = 0.4f))
                                        ) {
                                            Text(
                                                text = if (isWorker) "COOPERATIVE ARTISAN" else "VERIFIED CUSTOMER",
                                                color = ElegantLavender,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        if (isWorker && workerProfile?.verificationStatus == "VERIFIED") {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Verified,
                                                    contentDescription = "Verified",
                                                    tint = ElegantMintGreen,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = "NSDC Verified",
                                                    fontSize = 10.sp,
                                                    color = ElegantMintGreen,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Cooperative Emblem Icon
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Cooperative Emblem",
                                tint = ElegantLavender.copy(alpha = 0.5f),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Immutable ID Ribbon
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ElegantDarkBg,
                            border = BorderStroke(1.dp, ElegantDarkBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (isWorker) "IMMUTABLE WORKER ID" else "OFFICIAL CUSTOMER ID",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ElegantTextSecondary,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = officialId,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ElegantLavender,
                                        letterSpacing = 1.sp
                                    )
                                }

                                IconButton(
                                    onClick = { copyToClipboard(officialId, "ID") },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy ID",
                                        tint = ElegantTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Metrics Ribbon (Workers get rating, jobs, wage; Customers get booking count)
                        if (isWorker) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetricPill(
                                    label = "Rating",
                                    value = "★ ${workerProfile?.customerRating ?: 4.9} (${workerProfile?.ratingCount ?: 34})",
                                    color = ElegantMintGreen,
                                    modifier = Modifier.weight(1f)
                                )
                                MetricPill(
                                    label = "Completed Jobs",
                                    value = "${workerProfile?.completedJobs ?: 128}",
                                    color = ElegantLavender,
                                    modifier = Modifier.weight(1f)
                                )
                                MetricPill(
                                    label = "Base Wage",
                                    value = "₹$dailyWageRate/day",
                                    color = ElegantSaffron,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetricPill(
                                    label = "Account Tier",
                                    value = "Cooperative Resident",
                                    color = ElegantLavender,
                                    modifier = Modifier.weight(1f)
                                )
                                MetricPill(
                                    label = "Location",
                                    value = "$district, $state",
                                    color = ElegantMintGreen,
                                    modifier = Modifier.weight(1f)
                                )
                                MetricPill(
                                    label = "Status",
                                    value = "Active Member",
                                    color = ElegantSoftBlue,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Profile Section Navigation Tabs ---
        item {
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = ElegantDarkSurface,
                contentColor = ElegantLavender,
                indicator = { tabPositions ->
                    if (activeTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                            color = ElegantLavender
                        )
                    }
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, ElegantDarkBorder, RoundedCornerShape(14.dp))
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = {
                        Text(
                            text = if (isWorker) "Skills & Trade" else "Service Preferences",
                            fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (activeTab == 0) ElegantLavender else ElegantTextSecondary
                        )
                    },
                    modifier = Modifier.testTag("tab_skills_trade")
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        Text(
                            text = "Contact & Address",
                            fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (activeTab == 1) ElegantLavender else ElegantTextSecondary
                        )
                    },
                    modifier = Modifier.testTag("tab_contact_address")
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = {
                        Text(
                            text = "Cooperative & Security",
                            fontWeight = if (activeTab == 2) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (activeTab == 2) ElegantLavender else ElegantTextSecondary
                        )
                    },
                    modifier = Modifier.testTag("tab_security_coop")
                )
            }
        }

        // --- TAB 0: PROFESSIONAL DETAILS & SKILLS ---
        if (activeTab == 0) {
            if (isWorker) {
                // WORKER: Primary Trade & Daily Wage Rate
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                        border = BorderStroke(1.dp, ElegantDarkBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "PRIMARY TRADE & LABOR RATE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElegantLavender,
                                letterSpacing = 1.sp
                            )

                            if (isEditing) {
                                Text(
                                    text = "Select Primary Profession:",
                                    fontSize = 12.sp,
                                    color = ElegantTextSecondary
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(availableTrades) { trade ->
                                        val isSelected = trade.equals(primarySkill, ignoreCase = true)
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { primarySkill = trade },
                                            label = { Text(trade, fontSize = 12.sp) },
                                            leadingIcon = {
                                                if (isSelected) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = ElegantLavender,
                                                selectedLabelColor = ElegantOnLavender,
                                                containerColor = ElegantDarkSurfaceVariant,
                                                labelColor = ElegantTextPrimary
                                            )
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = dailyWageRate,
                                        onValueChange = { dailyWageRate = it.filter { ch -> ch.isDigit() } },
                                        label = { Text("Base Wage (₹/day)") },
                                        leadingIcon = {
                                            Text(
                                                "₹",
                                                color = ElegantLavender,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = ElegantLavender,
                                            unfocusedBorderColor = ElegantDarkBorder
                                        )
                                    )

                                    OutlinedTextField(
                                        value = experienceYears.toString(),
                                        onValueChange = {
                                            experienceYears = it.filter { ch -> ch.isDigit() }.toIntOrNull() ?: 1
                                        },
                                        label = { Text("Experience (Years)") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Timeline,
                                                contentDescription = null,
                                                tint = ElegantLavender,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = ElegantLavender,
                                            unfocusedBorderColor = ElegantDarkBorder
                                        )
                                    )
                                }
                            } else {
                                ProfileDetailRow("Primary Trade", primarySkill)
                                ProfileDetailRow("Experience", "$experienceYears years in craft")
                                ProfileDetailRow("Standard Labor Rate", "₹$dailyWageRate / day (Fair cooperative benchmark)")
                            }
                        }
                    }
                }

                // WORKER: Secondary Skills Portfolio
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                        border = BorderStroke(1.dp, ElegantDarkBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SECONDARY SKILLS PORTFOLIO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElegantLavender,
                                    letterSpacing = 1.sp
                                )

                                if (isEditing) {
                                    TextButton(
                                        onClick = { showAddSkillDialog = true },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Add Skill",
                                            tint = ElegantLavender,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Add Skill",
                                            fontSize = 12.sp,
                                            color = ElegantLavender,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Dynamic Skill Chips
                            if (secondarySkillList.isEmpty()) {
                                Text(
                                    text = "No secondary skills listed. Tap 'Edit Profile' to add certifications and trades.",
                                    fontSize = 12.sp,
                                    color = ElegantTextMuted
                                )
                            } else {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    secondarySkillList.forEach { skill ->
                                        AssistChip(
                                            onClick = {
                                                if (isEditing) {
                                                    secondarySkillList = secondarySkillList.filter { it != skill }
                                                }
                                            },
                                            label = { Text(skill, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                                            trailingIcon = {
                                                if (isEditing) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Remove skill",
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            },
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = ElegantDarkSurfaceVariant,
                                                labelColor = ElegantTextPrimary
                                            ),
                                            border = BorderStroke(1.dp, ElegantLavender.copy(alpha = 0.3f))
                                        )
                                    }
                                }
                            }

                            if (isEditing) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Quick add recommended skills:",
                                    fontSize = 11.sp,
                                    color = ElegantTextSecondary
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(popularSkillTags.filter { it !in secondarySkillList }) { recSkill ->
                                        SuggestionChip(
                                            onClick = {
                                                secondarySkillList = secondarySkillList + recSkill
                                            },
                                            label = { Text("+ $recSkill", fontSize = 11.sp) },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = ElegantDarkSurfaceHighlight
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // WORKER: Certifications & Availability Toggles
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                        border = BorderStroke(1.dp, ElegantDarkBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "ACCREDITATIONS & SERVICE STATUS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElegantLavender,
                                letterSpacing = 1.sp
                            )

                            if (isEditing) {
                                OutlinedTextField(
                                    value = certifications,
                                    onValueChange = { certifications = it },
                                    label = { Text("Certifications / Accreditations") },
                                    placeholder = { Text("e.g. NSDC Level 4, ITI Certified, Safety Training") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ElegantLavender,
                                        unfocusedBorderColor = ElegantDarkBorder
                                    )
                                )

                                OutlinedTextField(
                                    value = zone,
                                    onValueChange = { zone = it },
                                    label = { Text("Operating City Zone / Cluster") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ElegantLavender,
                                        unfocusedBorderColor = ElegantDarkBorder
                                    )
                                )
                            } else {
                                ProfileDetailRow("Certifications", certifications.ifBlank { "Standard Skill Verification" })
                                ProfileDetailRow("Operational Zone", zone)
                            }

                            HorizontalDivider(color = ElegantDarkBorder, thickness = 1.dp)

                            // Availability Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "On-Duty Availability",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = ElegantTextWhite
                                    )
                                    Text(
                                        text = if (isAvailable) "You appear in customer searches for instant booking" else "Profile set to offline / resting",
                                        fontSize = 11.sp,
                                        color = ElegantTextSecondary
                                    )
                                }
                                Switch(
                                    checked = isAvailable,
                                    onCheckedChange = { isAvailable = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = ElegantLavender,
                                        checkedTrackColor = ElegantLavenderContainer,
                                        uncheckedThumbColor = ElegantTextMuted,
                                        uncheckedTrackColor = ElegantDarkSurfaceVariant
                                    )
                                )
                            }

                            // Emergency Ready Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "24/7 Emergency Dispatch Ready",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = ElegantTextWhite
                                    )
                                    Text(
                                        text = "Receive high-priority SOS emergency work requests (+25% labor bonus)",
                                        fontSize = 11.sp,
                                        color = ElegantTextSecondary
                                    )
                                }
                                Switch(
                                    checked = isEmergencyReady,
                                    onCheckedChange = { isEmergencyReady = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = ElegantEmergencyRed,
                                        checkedTrackColor = ElegantEmergencyDarkContainer,
                                        uncheckedThumbColor = ElegantTextMuted,
                                        uncheckedTrackColor = ElegantDarkSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                // CUSTOMER: Service Preferences
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                        border = BorderStroke(1.dp, ElegantDarkBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "FREQUENTLY REQUESTED SERVICES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElegantLavender,
                                letterSpacing = 1.sp
                            )

                            Text(
                                text = "Select your preferred artisan categories for rapid one-tap home dispatch:",
                                fontSize = 12.sp,
                                color = ElegantTextSecondary
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                availableTrades.forEach { trade ->
                                    val isSelected = trade in customerPreferredSkills
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            if (isEditing) {
                                                customerPreferredSkills = if (isSelected) {
                                                    customerPreferredSkills - trade
                                                } else {
                                                    customerPreferredSkills + trade
                                                }
                                            }
                                        },
                                        label = { Text(trade, fontSize = 12.sp) },
                                        leadingIcon = {
                                            if (isSelected) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = ElegantLavender,
                                            selectedLabelColor = ElegantOnLavender,
                                            containerColor = ElegantDarkSurfaceVariant,
                                            labelColor = ElegantTextPrimary
                                        )
                                    )
                                }
                            }

                            HorizontalDivider(color = ElegantDarkBorder, thickness = 1.dp)

                            Text(
                                text = "DEFAULT SERVICE NOTES FOR ARTISANS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElegantLavender,
                                letterSpacing = 1.sp
                            )

                            if (isEditing) {
                                OutlinedTextField(
                                    value = customerNotes,
                                    onValueChange = { customerNotes = it },
                                    label = { Text("Service Instructions") },
                                    placeholder = { Text("e.g. Ring bell, elevator available, preferred timings") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ElegantLavender,
                                        unfocusedBorderColor = ElegantDarkBorder
                                    )
                                )
                            } else {
                                Text(
                                    text = customerNotes.ifBlank { "No default instructions set." },
                                    fontSize = 13.sp,
                                    color = ElegantTextWhite
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- TAB 1: CONTACT INFORMATION & ADDRESS ---
        if (activeTab == 1) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "COMMUNICATION & CONTACT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElegantLavender,
                            letterSpacing = 1.sp
                        )

                        if (isEditing) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Full Legal Name") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = ElegantLavender)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElegantLavender,
                                    unfocusedBorderColor = ElegantDarkBorder
                                )
                            )

                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Primary Phone (Verified)") },
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = ElegantLavender)
                                },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = "Verified Mobile",
                                        tint = ElegantMintGreen
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElegantLavender,
                                    unfocusedBorderColor = ElegantDarkBorder
                                )
                            )

                            OutlinedTextField(
                                value = alternatePhone,
                                onValueChange = { alternatePhone = it },
                                label = { Text("Alternate / WhatsApp Number") },
                                leadingIcon = {
                                    Icon(Icons.Default.ContactPhone, contentDescription = null, tint = ElegantLavender)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElegantLavender,
                                    unfocusedBorderColor = ElegantDarkBorder
                                )
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email Address") },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = ElegantLavender)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElegantLavender,
                                    unfocusedBorderColor = ElegantDarkBorder
                                )
                            )
                        } else {
                            ProfileDetailRow("Full Name", name)
                            ProfileDetailRow("Primary Mobile", "$phone (OTP Verified)")
                            ProfileDetailRow("Alternate / WhatsApp", alternatePhone)
                            ProfileDetailRow("Email", email)
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "ADDRESS & GEOGRAPHIC REGISTRY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElegantLavender,
                            letterSpacing = 1.sp
                        )

                        if (isEditing) {
                            OutlinedTextField(
                                value = village,
                                onValueChange = { village = it },
                                label = { Text("Street / Colony / Landmark") },
                                leadingIcon = {
                                    Icon(Icons.Default.Home, contentDescription = null, tint = ElegantLavender)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElegantLavender,
                                    unfocusedBorderColor = ElegantDarkBorder
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = city,
                                    onValueChange = { city = it },
                                    label = { Text("City") },
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ElegantLavender,
                                        unfocusedBorderColor = ElegantDarkBorder
                                    )
                                )
                                OutlinedTextField(
                                    value = district,
                                    onValueChange = { district = it },
                                    label = { Text("District") },
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ElegantLavender,
                                        unfocusedBorderColor = ElegantDarkBorder
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = state,
                                    onValueChange = { state = it },
                                    label = { Text("State") },
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ElegantLavender,
                                        unfocusedBorderColor = ElegantDarkBorder
                                    )
                                )
                                OutlinedTextField(
                                    value = "800001",
                                    onValueChange = { },
                                    label = { Text("PIN Code") },
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ElegantLavender,
                                        unfocusedBorderColor = ElegantDarkBorder
                                    )
                                )
                            }
                        } else {
                            ProfileDetailRow("Street / Landmark", village.ifBlank { "Bailey Road, Near Central Plaza" })
                            ProfileDetailRow("City / District", "$city, $district")
                            ProfileDetailRow("State & PIN", "$state - 800001")
                            ProfileDetailRow("Jurisdiction", "Bihar State Artisan & Cooperative Registry")
                        }
                    }
                }
            }
        }

        // --- TAB 2: COOPERATIVE MEMBERSHIP & SECURITY ---
        if (activeTab == 2) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "COOPERATIVE SOCIETY AFFILIATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElegantLavender,
                            letterSpacing = 1.sp
                        )

                        ProfileDetailRow(
                            "Affiliated Cooperative",
                            workerProfile?.cooperativeName ?: "Patna Central Artisans Multipurpose Cooperative Society Ltd."
                        )
                        ProfileDetailRow("Registration No.", "COOP-BIH-PAT-2018-0042")
                        ProfileDetailRow("District Branch", "Exhibition Road, Patna 800001")
                        ProfileDetailRow("Welfare Entitlement", "PM-JJBY, PMSYM & Emergency Medical Advance Active")
                    }
                }
            }

            // Security & Password Change
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ACCOUNT SECURITY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElegantLavender,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Manage your authentication credentials",
                                    fontSize = 12.sp,
                                    color = ElegantTextSecondary
                                )
                            }

                            FilledTonalButton(
                                onClick = { isChangingPassword = !isChangingPassword },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = ElegantLavender.copy(alpha = 0.15f),
                                    contentColor = ElegantLavender
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isChangingPassword) Icons.Default.Close else Icons.Default.Key,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isChangingPassword) "Cancel" else "Change Password",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = isChangingPassword,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = oldPassword,
                                    onValueChange = { oldPassword = it },
                                    label = { Text("Current Password") },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ElegantLavender,
                                        unfocusedBorderColor = ElegantDarkBorder
                                    )
                                )

                                OutlinedTextField(
                                    value = newPassword,
                                    onValueChange = { newPassword = it },
                                    label = { Text("New Password (min 6 characters)") },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ElegantLavender,
                                        unfocusedBorderColor = ElegantDarkBorder
                                    )
                                )

                                OutlinedTextField(
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it },
                                    label = { Text("Confirm New Password") },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ElegantLavender,
                                        unfocusedBorderColor = ElegantDarkBorder
                                    )
                                )

                                passwordError?.let { err ->
                                    Text(
                                        text = err,
                                        color = ElegantEmergencyRed,
                                        fontSize = 12.sp
                                    )
                                }

                                Button(
                                    onClick = {
                                        if (newPassword.length < 6) {
                                            passwordError = "Password must be at least 6 characters."
                                        } else if (newPassword != confirmPassword) {
                                            passwordError = "New passwords do not match."
                                        } else {
                                            onChangePassword?.invoke(oldPassword, newPassword)
                                            isChangingPassword = false
                                            oldPassword = ""
                                            newPassword = ""
                                            confirmPassword = ""
                                            passwordError = null
                                            feedbackMessage = "Password updated successfully"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElegantLavender),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        "Save New Password",
                                        color = ElegantOnLavender,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Logout Button
            if (onLogout != null) {
                item {
                    OutlinedButton(
                        onClick = onLogout,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantEmergencyRed),
                        border = BorderStroke(1.dp, ElegantEmergencyRed.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_logout_profile")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Log Out from Sahakaar Setu",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    // Modal Dialog to Add Custom Skill
    if (showAddSkillDialog) {
        AlertDialog(
            onDismissRequest = { showAddSkillDialog = false },
            title = {
                Text(
                    "Add Trade Skill",
                    fontWeight = FontWeight.Bold,
                    color = ElegantTextWhite
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Enter a skill or certification to showcase on your profile:",
                        fontSize = 12.sp,
                        color = ElegantTextSecondary
                    )
                    OutlinedTextField(
                        value = newCustomSkillText,
                        onValueChange = { newCustomSkillText = it },
                        label = { Text("Skill Name") },
                        placeholder = { Text("e.g. Submersible Motor, Solar Inverter") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElegantLavender,
                            unfocusedBorderColor = ElegantDarkBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newCustomSkillText.trim()
                        if (trimmed.isNotBlank() && trimmed !in secondarySkillList) {
                            secondarySkillList = secondarySkillList + trimmed
                        }
                        newCustomSkillText = ""
                        showAddSkillDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElegantLavender)
                ) {
                    Text("Add", color = ElegantOnLavender, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSkillDialog = false }) {
                    Text("Cancel", color = ElegantTextSecondary)
                }
            },
            containerColor = ElegantDarkSurface,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun MetricPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = ElegantDarkBg,
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier.padding(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = ElegantTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProfileDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = ElegantTextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = ElegantTextWhite,
            modifier = Modifier.weight(1.5f)
        )
    }
}
