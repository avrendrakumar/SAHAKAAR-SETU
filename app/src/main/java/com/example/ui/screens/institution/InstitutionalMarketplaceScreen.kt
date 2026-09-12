package com.example.ui.screens.institution

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.CooperativeEntity
import com.example.data.model.InstitutionalBookingEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstitutionalMarketplaceScreen(
    institutionalBookings: List<InstitutionalBookingEntity>,
    cooperatives: List<CooperativeEntity>,
    language: AppLanguage,
    onSubmitRequest: (String, String, String, Int, Int, String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showRequestDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ElegantDarkBg)
    ) {
        // Banner Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Business, contentDescription = "B2B", tint = ElegantLavender, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Institutional Marketplace",
                                color = ElegantTextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bulk workforce deployment for Schools, Hospitals, Offices & Municipalities",
                            color = ElegantTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { showRequestDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantLavender,
                            contentColor = ElegantOnLavender
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Post Request", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Institutional Benefits Strip
        Surface(
            color = ElegantDarkSurfaceVariant,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("✓ GST Compliance", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ElegantLavender)
                Text("✓ Biometric Attendance", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ElegantMintGreen)
                Text("✓ Supervisor", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ElegantLavender)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Requests List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Active Deployments (${institutionalBookings.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = ElegantTextWhite
                )
            }

            if (institutionalBookings.isEmpty()) {
                item {
                    Text(
                        text = "No institutional deployment requests posted yet. Use 'Post Request' above to commission bulk workforce.",
                        fontSize = 12.sp,
                        color = ElegantTextSecondary,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(institutionalBookings) { req ->
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = req.institutionName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = ElegantTextWhite
                                )
                                Text(
                                    text = "${req.institutionType} • ${req.locationCity}",
                                    fontSize = 11.sp,
                                    color = ElegantTextSecondary
                                )
                            }

                            Surface(
                                color = if (req.status == "ALLOCATED") ElegantMintGreen.copy(alpha = 0.15f) else ElegantLavender.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (req.status == "ALLOCATED") ElegantMintGreen.copy(alpha = 0.4f) else ElegantLavender.copy(alpha = 0.4f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = req.status,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (req.status == "ALLOCATED") ElegantMintGreen else ElegantLavender,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = ElegantDarkSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Workforce", fontSize = 10.sp, color = ElegantTextMuted)
                                    Text("${req.workerCount} ${req.requiredSkill}s", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ElegantLavender)
                                }
                            }
                            Surface(
                                color = ElegantDarkSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Duration", fontSize = 10.sp, color = ElegantTextMuted)
                                    Text("${req.durationDays} Days", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ElegantTextWhite)
                                }
                            }
                            Surface(
                                color = ElegantDarkSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Budget", fontSize = 10.sp, color = ElegantTextMuted)
                                    Text("₹${req.estimatedBudget.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ElegantMintGreen)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Point of Contact: ${req.contactPerson} (${req.contactPhone})",
                            fontSize = 11.sp,
                            color = ElegantTextSecondary
                        )
                    }
                }
            }
            }
        }
    }

    // Post Institutional Request Dialog
    if (showRequestDialog) {
        var instName by remember { mutableStateOf("") }
        var instType by remember { mutableStateOf("SCHOOL") }
        var reqTrade by remember { mutableStateOf("Electrical") }
        var numWorkers by remember { mutableStateOf("6") }
        var numDays by remember { mutableStateOf("3") }
        var city by remember { mutableStateOf("Patna") }
        var contactPerson by remember { mutableStateOf("") }
        var contactPhone by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showRequestDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                modifier = Modifier.fillMaxWidth().padding(4.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Bulk Workforce Request", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ElegantTextWhite)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = instName,
                        onValueChange = { instName = it },
                        label = { Text("Institution / Enterprise Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ElegantTextWhite,
                            unfocusedTextColor = ElegantTextWhite,
                            focusedBorderColor = ElegantLavender,
                            unfocusedBorderColor = ElegantDarkBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Institution Type:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ElegantTextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("SCHOOL", "HOSPITAL", "OFFICE", "HOTEL", "SOCIETY", "FACTORY").forEach { t ->
                            FilterChip(
                                selected = instType == t,
                                onClick = { instType = t },
                                label = { Text(t, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElegantLavender,
                                    selectedLabelColor = ElegantOnLavender,
                                    containerColor = ElegantDarkSurfaceVariant,
                                    labelColor = ElegantTextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = instType == t,
                                    borderColor = ElegantDarkBorder,
                                    selectedBorderColor = ElegantLavender
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Trade Skill:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ElegantTextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Electrical", "Plumbing", "Carpentry", "Masonry", "General Labour").forEach { tr ->
                            FilterChip(
                                selected = reqTrade == tr,
                                onClick = { reqTrade = tr },
                                label = { Text(tr, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElegantLavender,
                                    selectedLabelColor = ElegantOnLavender,
                                    containerColor = ElegantDarkSurfaceVariant,
                                    labelColor = ElegantTextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = reqTrade == tr,
                                    borderColor = ElegantDarkBorder,
                                    selectedBorderColor = ElegantLavender
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = numWorkers,
                            onValueChange = { numWorkers = it },
                            label = { Text("Workers Count") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ElegantTextWhite,
                                unfocusedTextColor = ElegantTextWhite,
                                focusedBorderColor = ElegantLavender,
                                unfocusedBorderColor = ElegantDarkBorder
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = numDays,
                            onValueChange = { numDays = it },
                            label = { Text("Days") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ElegantTextWhite,
                                unfocusedTextColor = ElegantTextWhite,
                                focusedBorderColor = ElegantLavender,
                                unfocusedBorderColor = ElegantDarkBorder
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = contactPerson,
                        onValueChange = { contactPerson = it },
                        label = { Text("Contact Person Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ElegantTextWhite,
                            unfocusedTextColor = ElegantTextWhite,
                            focusedBorderColor = ElegantLavender,
                            unfocusedBorderColor = ElegantDarkBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = { Text("Contact Phone") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ElegantTextWhite,
                            unfocusedTextColor = ElegantTextWhite,
                            focusedBorderColor = ElegantLavender,
                            unfocusedBorderColor = ElegantDarkBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { showRequestDialog = false },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantTextSecondary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                onSubmitRequest(
                                    if (instName.isNotBlank()) instName else "Enterprise Corp",
                                    instType,
                                    reqTrade,
                                    numWorkers.toIntOrNull() ?: 5,
                                    numDays.toIntOrNull() ?: 2,
                                    city,
                                    if (contactPerson.isNotBlank()) contactPerson else "Admin In-charge",
                                    if (contactPhone.isNotBlank()) contactPhone else "+91 94310 11223",
                                    cooperatives.firstOrNull()?.id ?: "coop_patna"
                                )
                                showRequestDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElegantLavender,
                                contentColor = ElegantOnLavender
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Submit Bulk Order")
                        }
                    }
                }
            }
        }
    }
}
