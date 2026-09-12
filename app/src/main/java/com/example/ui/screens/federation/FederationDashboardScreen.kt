package com.example.ui.screens.federation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.*
import com.example.ui.components.LiveWorkerMap
import com.example.ui.theme.*

@Composable
fun FederationDashboardScreen(
    currentAdmin: UserEntity,
    cooperatives: List<CooperativeEntity>,
    allWorkers: List<WorkerProfileEntity>,
    allBookings: List<BookingEntity>,
    demandForecasts: List<DemandForecastEntity>,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val totalWorkers = allWorkers.size
    val totalCooperatives = cooperatives.size
    val totalGrossVolume = allBookings.sumOf { it.totalAmount }
    val totalWelfareDisbursed = totalGrossVolume * 0.10

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ElegantDarkBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalance, contentDescription = "Federation", tint = ElegantLavender, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "State Federation Directorate",
                            color = ElegantTextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Bihar State Labour Cooperatives Federation • Patna HQ",
                        color = ElegantLavender,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Governance, Cross-District Allocation & Cooperative Compliance Monitoring",
                        color = ElegantTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // State-level Macro KPIs
        item {
            Text("Statewide Cooperative Macro Indicators", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ElegantTextWhite)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MacroCard("Affiliated Co-ops", "$totalCooperatives", "5 Districts Active", ElegantLavender, Modifier.weight(1f))
                MacroCard("State Artisan Pool", "$totalWorkers", "100% Certified", ElegantMintGreen, Modifier.weight(1f))
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MacroCard("Gross Work Volume", "₹${"%,.0f".format(totalGrossVolume)}", "Fair Wage Flow", ElegantLavender, Modifier.weight(1f))
                MacroCard("State Welfare Reserve", "₹${"%,.0f".format(totalWelfareDisbursed)}", "Health & Accident Shield", ElegantMintGreen, Modifier.weight(1f))
            }
        }

        // Live Statewide Worker & Hub Map
        item {
            Text("Statewide Real-time Artisan Mobility & Demand Heatmap", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ElegantTextWhite)
            Spacer(modifier = Modifier.height(6.dp))
            LiveWorkerMap(workers = allWorkers, cooperatives = cooperatives)
        }

        // Affiliated Cooperatives Performance
        item {
            Text("District Cooperatives Roster", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ElegantTextWhite)
        }

        items(cooperatives) { coop ->
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(coop.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ElegantTextWhite)
                        Text("Reg: ${coop.registrationNumber} • District: ${coop.district}", fontSize = 11.sp, color = ElegantTextSecondary)
                        Text("Contact: ${coop.contactPhone} • Estd: ${coop.establishedYear}", fontSize = 11.sp, color = ElegantTextSecondary)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("${coop.totalWorkers} Members", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = ElegantLavender)
                        Text("★ ${coop.rating} Audit", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ElegantMintGreen)
                    }
                }
            }
        }

        // Cross-District Skill Shortage Analysis
        item {
            Text("Cross-District Skill Shortage & Mobility Directives", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ElegantTextWhite)
        }

        items(demandForecasts) { df ->
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
                        Text("${df.district}: ${df.serviceCategory}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ElegantTextWhite)
                        Text("Shortage: -${df.shortage}", color = ElegantEmergencyRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Text("Recommendation: ${df.recommendation}", fontSize = 12.sp, color = ElegantTextSecondary, modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
    }
}

@Composable
private fun MacroCard(title: String, value: String, subtitle: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontSize = 11.sp, color = ElegantTextSecondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 10.sp, color = ElegantTextMuted)
        }
    }
}

